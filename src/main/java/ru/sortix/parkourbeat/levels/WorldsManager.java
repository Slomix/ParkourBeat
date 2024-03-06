package ru.sortix.parkourbeat.levels;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.levels.gen.EmptyChunkGenerator;
import ru.sortix.parkourbeat.utils.java.CopyDirVisitor;
import ru.sortix.parkourbeat.utils.shedule.BukkitAsyncExecutor;
import ru.sortix.parkourbeat.utils.shedule.BukkitSyncExecutor;
import ru.sortix.parkourbeat.utils.shedule.CurrentThreadExecutor;

public class WorldsManager {
    private final Logger logger;
    private final Server server;
    @Getter private final ChunkGenerator emptyGenerator;
    @Getter private final Executor currentThreadExecutor;
    @Getter private final Executor syncExecutor;
    @Getter private final Executor asyncExecutor;

    public WorldsManager(@NonNull Plugin plugin) {
        this.logger = plugin.getLogger();
        this.server = plugin.getServer();
        this.emptyGenerator = new EmptyChunkGenerator(this.server);
        this.currentThreadExecutor = new CurrentThreadExecutor();
        this.syncExecutor = new BukkitSyncExecutor(plugin);
        this.asyncExecutor = new BukkitAsyncExecutor(plugin);
    }

    @NonNull public CompletableFuture<World> createWorldFromDefaultContainer(
            @NonNull WorldCreator worldCreator, @NonNull Executor executor) {
        File worldDir = this.getWorldDir(worldCreator);
        if (!worldDir.isDirectory()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Is not a directory: " + worldDir.getAbsolutePath()));
        }
        return this.createBukkitWorld(worldCreator, executor);
    }

    @NonNull public CompletableFuture<World> createWorldFromCustomDirectory(
            @NonNull WorldCreator worldCreator, @NonNull File worldDir) {
        CompletableFuture<World> result = new CompletableFuture<>();

        File realWorldDir = this.getWorldDir(worldCreator);

        this.copyWorldData(worldDir, realWorldDir)
                .thenAccept(
                        dataPrepared -> {
                            if (!dataPrepared) {
                                result.completeExceptionally(
                                        new IllegalArgumentException("Unable to prepare world data"));
                                return;
                            }
                            this.createBukkitWorld(worldCreator, this.syncExecutor)
                                    .thenAccept(
                                            world -> {
                                                if (world == null) {
                                                    result.completeExceptionally(
                                                            new IllegalArgumentException("Unable to create Bukkit world"));
                                                    return;
                                                }
                                                result.complete(world);
                                            });
                        });
        return result;
    }

    @NonNull private CompletableFuture<Boolean> copyWorldData(@NonNull File source, @NonNull File target) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        if (target.exists()) throw new IOException("Target directory already exist");
                        CopyDirVisitor visitor =
                                new CopyDirVisitor(this.logger, source.toPath(), target.toPath());
                        Files.walkFileTree(source.toPath(), visitor);
                        return !visitor.isFailed();
                    } catch (IOException e) {
                        return false;
                    }
                },
                this.asyncExecutor);
    }

    @NonNull private CompletableFuture<World> createBukkitWorld(
            @NonNull WorldCreator worldCreator, @NonNull Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> {
                    World world = this.server.getWorld(worldCreator.name());
                    if (world != null) return world;

                    world = this.server.createWorld(worldCreator);
                    if (world != null) return world;

                    throw new UnsupportedOperationException(
                            "Unable to create world \""
                                    + worldCreator.name()
                                    + "\""
                                    + " from directory "
                                    + this.getWorldDir(worldCreator).getAbsolutePath());
                },
                executor);
    }

    @NonNull private File getWorldDir(@NonNull WorldCreator worldCreator) {
        if (false) {
            // Incorrect impl from CraftBukkit
            return new File(this.server.getWorldContainer(), worldCreator.name());
        }
        // Correct impl from NMS
        return this.server.getWorldContainer().toPath().resolve(worldCreator.name()).toFile();
    }
}
