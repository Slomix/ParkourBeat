package ru.sortix.parkourbeat.world;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import ru.sortix.parkourbeat.levels.gen.EmptyChunkGenerator;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.utils.java.CopyDirVisitor;
import ru.sortix.parkourbeat.utils.shedule.BukkitAsyncExecutor;
import ru.sortix.parkourbeat.utils.shedule.BukkitSyncExecutor;
import ru.sortix.parkourbeat.utils.shedule.CurrentThreadExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorldsManager implements PluginManager, Listener {
    private final Plugin plugin;
    private final Logger logger;
    private final Server server;

    @Getter
    private final ChunkGenerator emptyGenerator;

    @Getter
    private final Executor currentThreadExecutor;

    @Getter
    private final Executor syncExecutor;

    @Getter
    private final Executor asyncExecutor;

    private final Map<World, UnloadingWorld> unloadingWorlds = new HashMap<>();

    private record UnloadingWorld(@NonNull List<CompletableFuture<Boolean>> futures) {
        public void complete(boolean success) {
            for (CompletableFuture<Boolean> future : this.futures) {
                future.complete(success);
            }
        }
    }

    public WorldsManager(@NonNull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.server = plugin.getServer();
        this.emptyGenerator = new EmptyChunkGenerator(this.server);
        this.currentThreadExecutor = new CurrentThreadExecutor();
        this.syncExecutor = new BukkitSyncExecutor(plugin);
        this.asyncExecutor = new BukkitAsyncExecutor(plugin);
        this.server.getPluginManager().registerEvents(this, plugin);
    }

    @NonNull
    private static Set<Plugin> getHandlingPlugins(@NonNull HandlerList eventHandlerList) {
        Set<Plugin> plugins = new HashSet<>();
        for (RegisteredListener registeredListener : eventHandlerList.getRegisteredListeners()) {
            Plugin plugin = registeredListener.getPlugin();
            if (!plugin.isEnabled()) continue;
            plugins.add(plugin);
        }
        return plugins;
    }

    @NonNull
    public CompletableFuture<World> createWorldFromDefaultContainer(
        @NonNull WorldCreator worldCreator, @NonNull Executor executor) {
        File worldDir = this.getWorldDir(worldCreator);
        if (!worldDir.isDirectory()) {
            this.logger.severe("Unable to create world from directory (directory not found): " + worldDir);
            return CompletableFuture.completedFuture(null);
        }
        return this.createBukkitWorld(worldCreator, executor);
    }

    @NonNull
    public CompletableFuture<World> createWorldFromCustomDirectory(
        @NonNull WorldCreator worldCreator, @NonNull File worldDir) {
        CompletableFuture<World> result = new CompletableFuture<>();

        File realWorldDir = this.getWorldDir(worldCreator);

        this.copyWorldData(worldDir, realWorldDir).thenAccept(dataPrepared -> {
            if (!dataPrepared) {
                result.complete(null);
                return;
            }
            this.createBukkitWorld(worldCreator, this.syncExecutor).thenAccept(result::complete);
        });
        return result;
    }

    @NonNull
    private CompletableFuture<Boolean> copyWorldData(@NonNull File source, @NonNull File target) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    if (target.exists()) throw new IOException("Target directory already exist");
                    CopyDirVisitor visitor = new CopyDirVisitor(this.logger, source.toPath(), target.toPath());
                    Files.walkFileTree(source.toPath(), visitor);
                    return !visitor.isFailed();
                } catch (IOException e) {
                    this.logger.log(Level.SEVERE, "Unable to copy world data from " + source + " to " + target, e);
                    return false;
                }
            },
            this.asyncExecutor);
    }

    @NonNull
    private CompletableFuture<World> createBukkitWorld(@NonNull WorldCreator worldCreator, @NonNull Executor executor) {
        return CompletableFuture.supplyAsync(
            () -> {
                World world = this.server.getWorld(worldCreator.name());
                if (world != null) return world;

                try {
                    world = this.server.createWorld(worldCreator);
                    if (world != null) return world;
                    throw new IllegalArgumentException("Bukkit API result is null");
                } catch (Exception e) {
                    this.logger.log(
                        Level.SEVERE,
                        "Unable to create world \""
                            + worldCreator.name()
                            + "\""
                            + " from directory "
                            + this.getWorldDir(worldCreator).getAbsolutePath(),
                        e);
                }
                return null;
            },
            executor);
    }

    @NonNull
    private File getWorldDir(@NonNull WorldCreator worldCreator) {
        if (false) {
            // Incorrect impl from CraftBukkit
            return new File(this.server.getWorldContainer(), worldCreator.name());
        }
        // Correct impl from NMS
        return this.server
            .getWorldContainer()
            .toPath()
            .resolve(worldCreator.name())
            .toFile();
    }

    @NonNull
    public CompletableFuture<Boolean> unloadBukkitWorld(@NonNull World world,
                                                        boolean saveChunks,
                                                        @NonNull Location fallbackLocation,
                                                        boolean async
    ) {
        if (world == this.server.getWorlds().iterator().next()) {
            // world is default
            this.logger.severe("Не удалось отгрузить мир \"" + world.getName() + "\","
                + " т.к. он является основным миром сервера");
            return CompletableFuture.completedFuture(false);
        }

        if (this.server.getWorld(world.getName()) == null) {
            // world already unloaded
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        boolean unloadingStarted = this.unloadingWorlds.containsKey(world);
        this.unloadingWorlds
            .computeIfAbsent(world, world1 -> new UnloadingWorld(new ArrayList<>()))
            .futures.add(result);

        if (!unloadingStarted) {
            if (async) {
                this.startWorldUnloadingAsync(world, saveChunks, fallbackLocation);
            } else {
                this.startWorldUnloadingSync(world, saveChunks, fallbackLocation);
            }
        }

        return result;
    }

    private void startWorldUnloadingSync(@NonNull World world, boolean save, @NonNull Location fallbackLocation) {
        for (Player player : world.getPlayers()) {
            player.sendMessage("Мир, в котором вы находились, был отключён");
            TeleportUtils.teleportSync(this.plugin, player, fallbackLocation);
        }

        if (!world.getPlayers().isEmpty()) {
            this.logger.severe("Не удалось отгрузить мир \"" + world.getName() + "\","
                + " т.к. не удалось освободить его от игроков");
            UnloadingWorld unloadingWorld = this.unloadingWorlds.remove(world);
            if (unloadingWorld != null) unloadingWorld.complete(false);
            return;
        }

        this.server.unloadWorld(world, save); // call WorldUnloadEvent, result must be ignored
    }

    private void startWorldUnloadingAsync(@NonNull World world, boolean save, @NonNull Location fallbackLocation) {
        List<CompletableFuture<Boolean>> teleportationFutures = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            player.sendMessage("Мир, в котором вы находились, был отключён");
            teleportationFutures.add(TeleportUtils.teleportAsync(this.plugin, player, fallbackLocation));
        }

        CompletableFuture<Void> onPlayersTeleported;
        if (teleportationFutures.isEmpty()) {
            onPlayersTeleported = CompletableFuture.completedFuture(null);
        } else {
            onPlayersTeleported = CompletableFuture.allOf(teleportationFutures.toArray(new CompletableFuture[0]));
        }

        onPlayersTeleported.thenAcceptAsync(
            unused -> {

                if (!world.getPlayers().isEmpty()) {
                    this.logger.severe("Не удалось отгрузить мир \"" + world.getName() + "\","
                        + " т.к. не удалось освободить его от игроков");
                    UnloadingWorld unloadingWorld = this.unloadingWorlds.remove(world);
                    if (unloadingWorld != null) unloadingWorld.complete(false);
                    return;
                }

                this.server.unloadWorld(world, save); // call WorldUnloadEvent, result must be ignored
            },
            this.syncExecutor);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void on(WorldUnloadEvent event) {
        UnloadingWorld unloadingWorld = this.unloadingWorlds.remove(event.getWorld());
        if (unloadingWorld == null) return;
        boolean eventAllowed = !event.isCancelled();
        if (!eventAllowed) {
            this.logger.severe("Не удалось отгрузить мир \"" + event.getWorld().getName() + "\", "
                + "т.к. один из указанных плагинов отменил отгрузку мира в " + WorldUnloadEvent.class.getName()
                + ": "
                + getHandlingPlugins(WorldUnloadEvent.getHandlerList()).stream()
                .map(Plugin::getName)
                .collect(Collectors.joining(", ")));
        }
        unloadingWorld.complete(eventAllowed);
    }

    @EventHandler(ignoreCancelled = true)
    private void on(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() == event.getTo().getWorld()) return;
        if (!this.unloadingWorlds.containsKey(event.getTo().getWorld())) return;
        event.getPlayer().sendMessage("Мир, в который вы телепортируетесь, отключается...");
        event.setCancelled(true);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }
}
