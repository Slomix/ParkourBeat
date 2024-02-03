package ru.sortix.parkourbeat;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import ru.sortix.parkourbeat.commands.PlayCommand;
import ru.sortix.parkourbeat.commands.TpToWorldCommand;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.listeners.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.levels.dao.FileLevelSettingDAO;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.listeners.MoveListener;
import ru.sortix.parkourbeat.listeners.ResourcePackListener;
import ru.sortix.parkourbeat.listeners.SprintListener;
import ru.sortix.parkourbeat.location.Region;

import java.io.File;

public class ParkourBeat extends JavaPlugin {
    private static ParkourBeat plugin;

    public static ParkourBeat getInstance() {
        return plugin;
    }

    private LevelsManager levelsManager;

    public void onEnable() {
        plugin = this;
        Settings.load();
        ConfigurationSerialization.registerClass(Region.class);
        levelsManager = new LevelsManager(new FileLevelSettingDAO(getDataFolder() + File.separator + "settings"));

        PluginCommand tpToWorldCommand = getCommand("tptoworld");
        TpToWorldCommand command = new TpToWorldCommand();
        tpToWorldCommand.setExecutor(command);
        tpToWorldCommand.setTabCompleter(command);

        getCommand("play").setExecutor(new PlayCommand());

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new ResourcePackListener(), this);
        Bukkit.getPluginManager().registerEvents(new SprintListener(), this);
    }

    public void onDisable() {

    }

    public static LevelsManager getLevelsManager() {
        return plugin.levelsManager;
    }
}
