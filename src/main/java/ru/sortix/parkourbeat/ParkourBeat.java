package ru.sortix.parkourbeat;

import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.message.LiteMessages;
import dev.rollczi.litecommands.schematic.SchematicFormat;
import lombok.NonNull;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.commands.*;
import ru.sortix.parkourbeat.commands.argument.GameSettingsArgumentResolver;
import ru.sortix.parkourbeat.commands.handler.DefaultInvalidUsageHandler;
import ru.sortix.parkourbeat.constant.Messages;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.data.SongsManager;
import ru.sortix.parkourbeat.inventory.InventoriesListener;
import ru.sortix.parkourbeat.item.ItemsManager;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.WorldsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.listeners.FixesListener;
import ru.sortix.parkourbeat.listeners.GamesListener;
import ru.sortix.parkourbeat.listeners.WorldsListener;
import ru.sortix.parkourbeat.location.Waypoint;
import ru.sortix.parkourbeat.player.input.PlayersInputManager;
import ru.sortix.parkourbeat.utils.NonWorldAndYawPitchLocation;
import ru.sortix.parkourbeat.utils.NonWorldLocation;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class ParkourBeat extends JavaPlugin {
    public static JavaPlugin getPlugin() {
        return JavaPlugin.getPlugin(ParkourBeat.class);
    }

    private final Map<Class<?>, PluginManager> managers = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        for (Class<? extends ConfigurationSerializable> clazz : this.getConfigSerializableClasses()) {
            ConfigurationSerialization.registerClass(clazz);
        }

        this.registerManager(ItemsManager::new);
        this.registerManager(WorldsManager::new);
        this.registerManager(ActivityManager::new);
        Settings.load(this);
        this.registerManager(SongsManager::new);
        this.registerManager(LevelsManager::new);
        this.registerManager(PlayersInputManager::new);

        this.registerCommands();

        this.registerListener(FixesListener::new);
        this.registerListener(GamesListener::new);
        this.registerListener(WorldsListener::new);
        this.registerListener(InventoriesListener::new);
    }

    @Override
    public void onDisable() {
        if (false) {
            for (Class<? extends ConfigurationSerializable> clazz : this.getConfigSerializableClasses()) {
                ConfigurationSerialization.unregisterClass(clazz);
            }
        }

        List<PluginManager> managers = new ArrayList<>(this.managers.values());
        Collections.reverse(managers);
        for (PluginManager manager : managers) {
            try {
                manager.disable();
            } catch (Exception e) {
                this.getLogger()
                        .log(
                                Level.SEVERE,
                                "Unable to unload manager " + manager.getClass().getName(),
                                e);
            }
        }
        this.managers.clear();

        HandlerList.unregisterAll(this);
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
    private void registerCommands() {
        LiteBukkitFactory.builder(getName().toLowerCase(Locale.ROOT), this)
                .commands(
                        new CommandColor(),
                        new CommandConvertData(this),
                        new CommandCreate(this),
                        new CommandDelete(this),
                        new CommandEdit(this),
                        new CommandPlay(this),
                        new CommandSong(),
                        new CommandSpawn(this),
                        new CommandTest(),
                        new CommandTpToWorld(this))
                .argument(GameSettings.class, new GameSettingsArgumentResolver(get(LevelsManager.class)))
                .message(LiteBukkitMessages.PLAYER_ONLY, Messages.PLAYER_ONLY)
                .message(LiteMessages.MISSING_PERMISSIONS, Messages.MISSING_PERMISSION)
                .invalidUsage(new DefaultInvalidUsageHandler())
                .schematicGenerator(SchematicFormat.angleBrackets())
                .build();
    }

    private void registerListener(@NonNull Function<ParkourBeat, Listener> listenerConstructor) {
        Listener listener = listenerConstructor.apply(this);
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    @NonNull private Collection<Class<? extends ConfigurationSerializable>> getConfigSerializableClasses() {
        return Arrays.asList(NonWorldAndYawPitchLocation.class, NonWorldLocation.class, Waypoint.class);
    }

    @NonNull public <M extends PluginManager> M get(@NonNull Class<M> managerClass) {
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
