package ru.sortix.parkourbeat;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import lombok.NonNull;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.commands.*;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.data.SongsManager;
import ru.sortix.parkourbeat.inventory.InventoriesListener;
import ru.sortix.parkourbeat.item.ItemsManager;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.WorldsManager;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.listeners.GamesListener;
import ru.sortix.parkourbeat.listeners.WorldsListener;
import ru.sortix.parkourbeat.location.Waypoint;
import ru.sortix.parkourbeat.utils.NonWorldAndYawPitchLocation;
import ru.sortix.parkourbeat.utils.NonWorldLocation;

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

        this.registerCommand(CommandColor::new, "color");
        this.registerCommand(CommandConvertData::new, "convertdata");
        this.registerCommand(CommandCreate::new, "create");
        this.registerCommand(CommandDelete::new, "delete");
        this.registerCommand(CommandEdit::new, "edit");
        this.registerCommand(CommandPlay::new, "play");
        this.registerCommand(CommandSong::new, "song");
        this.registerCommand(CommandSpawn::new, "spawn");
        this.registerCommand(CommandTest::new, "test");
        this.registerCommand(CommandTpToWorld::new, "tptoworld");

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

        for (PluginManager manager : new ArrayList<>(this.managers.values()).reversed()) {
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

    public void registerManager(@NonNull Function<ParkourBeat, PluginManager> commandConstructor) {
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

    public void registerCommand(
            @NonNull Function<ParkourBeat, PluginCommand<?>> commandConstructor, @NonNull String commandName) {
        org.bukkit.command.PluginCommand command = getCommand(commandName);
        if (command == null) {
            this.getLogger().severe("Unable to register command " + commandName + ". Is it specified in plugin.yml?");
            return;
        }
        CommandExecutor executor = commandConstructor.apply(this);
        command.setExecutor(executor);
        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }
    }

    public void registerListener(@NonNull Function<ParkourBeat, Listener> listenerConstructor) {
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
