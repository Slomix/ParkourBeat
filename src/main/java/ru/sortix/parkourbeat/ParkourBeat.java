package ru.sortix.parkourbeat;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.argument.ArgumentKey;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.message.LiteMessages;
import dev.rollczi.litecommands.schematic.SchematicFormat;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.commands.*;
import ru.sortix.parkourbeat.commands.argument.GameSettingsArgumentResolver;
import ru.sortix.parkourbeat.commands.handler.DefaultInvalidUsageHandler;
import ru.sortix.parkourbeat.constant.Messages;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.inventory.InventoriesListener;
import ru.sortix.parkourbeat.item.ItemsManager;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.listeners.FixesListener;
import ru.sortix.parkourbeat.listeners.GamesListener;
import ru.sortix.parkourbeat.physics.CustomPhysicsManager;
import ru.sortix.parkourbeat.player.input.PlayersInputManager;
import ru.sortix.parkourbeat.player.music.MusicTracksManager;
import ru.sortix.parkourbeat.world.WorldsListener;
import ru.sortix.parkourbeat.world.WorldsManager;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class ParkourBeat extends JavaPlugin {
    private final Map<Class<?>, PluginManager> managers = new LinkedHashMap<>();

    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        this.registerAllManagers();
        Settings.load(this, this.get(WorldsManager.class), this.get(LevelsManager.class));
        this.registerAllCommands();
        this.registerAllListeners();
    }

    @Override
    public void onDisable() {
        this.unregisterAllListeners();
        this.unregisterAllCommands();
        this.unregisterAllManagers();
        Settings.unload();
    }

    private void registerAllManagers() {
        this.registerManager(ItemsManager::new);
        this.registerManager(WorldsManager::new);
        this.registerManager(ActivityManager::new);
        this.registerManager(MusicTracksManager::new);
        this.registerManager(LevelsManager::new);
        this.registerManager(PlayersInputManager::new);
        this.registerManager(CustomPhysicsManager::new);
    }

    private void registerManager(@NonNull Function<ParkourBeat, PluginManager> commandConstructor) {
        PluginManager manager;
        try {
            manager = commandConstructor.apply(this);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create manager", e);
        }
        if (this.managers.put(manager.getClass(), manager) != null) {
            throw new IllegalStateException("Duplicate manager with class " + manager.getClass());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerAllCommands() {
        liteCommands = LiteBukkitFactory.builder(getName().toLowerCase(Locale.ROOT), this)
            .commands(
                // Alphabet order
                new CommandConvertData(this),
                new CommandCreate(this),
                new CommandDelete(this),
                new CommandEdit(this),
                new CommandPhysicsDebug(this),
                new CommandPlay(this),
                new CommandSpawn(this),
                new CommandTest(this),
                new CommandTpToWorld(this),
                new CommandUpdateTrack(this)
                // Alphabet order
            )
            .argument(GameSettings.class, ArgumentKey.of("settings-console-owning"), new GameSettingsArgumentResolver(get(LevelsManager.class), false, true, true))
            .argument(GameSettings.class, ArgumentKey.of("settings-players-owning"), new GameSettingsArgumentResolver(get(LevelsManager.class), true, false, true))
            .argument(GameSettings.class, ArgumentKey.of("settings-players-all"), new GameSettingsArgumentResolver(get(LevelsManager.class), true, false, false))
            .message(LiteBukkitMessages.PLAYER_ONLY, Messages.PLAYER_ONLY)
            .message(LiteMessages.MISSING_PERMISSIONS, Messages.MISSING_PERMISSION)
            .invalidUsage(new DefaultInvalidUsageHandler())
            .schematicGenerator(SchematicFormat.angleBrackets())
            .build();
    }

    private void registerAllListeners() {
        this.registerListener(FixesListener::new);
        this.registerListener(GamesListener::new);
        this.registerListener(WorldsListener::new);
        this.registerListener(InventoriesListener::new);
    }

    private void registerListener(@NonNull Function<ParkourBeat, Listener> listenerConstructor) {
        Listener listener = listenerConstructor.apply(this);
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    private void unregisterAllManagers() {
        List<PluginManager> managersToDisable = new ArrayList<>(this.managers.values());
        Collections.reverse(managersToDisable);
        for (PluginManager manager : managersToDisable) {
            unregisterSafely(manager::disable);
        }
        this.managers.clear();
    }

    private void unregisterAllCommands() {
        unregisterSafely(() -> {
            if (liteCommands != null) {
                liteCommands.unregister();
                liteCommands = null;
            }
        });
    }

    private void unregisterAllListeners() {
        unregisterSafely(() -> HandlerList.unregisterAll(this));
    }

    private void unregisterSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            this.getLogger().log(Level.SEVERE, "An occurred error while disabling plugin", e);
        }
    }

    @NonNull
    public <M extends PluginManager> M get(@NonNull Class<M> managerClass) {
        Object manager = this.managers.get(managerClass);
        if (manager == null) {
            throw new IllegalArgumentException("Manager with class " + managerClass.getName() + " not found");
        }
        try {
            return managerClass.cast(manager);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(
                "Manager " + manager.getClass().getName() + " isn't " + managerClass.getName());
        }
    }
}
