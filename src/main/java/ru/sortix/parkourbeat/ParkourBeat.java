package ru.sortix.parkourbeat;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import ru.sortix.parkourbeat.commands.EditorCommand;
import ru.sortix.parkourbeat.commands.PlayCommand;
import ru.sortix.parkourbeat.commands.TpToWorldCommand;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.LevelEditorListener;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.listeners.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.levels.dao.files.FileLevelSettingDAO;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.listeners.MoveListener;
import ru.sortix.parkourbeat.listeners.ResourcePackListener;
import ru.sortix.parkourbeat.listeners.SprintListener;
import ru.sortix.parkourbeat.location.Waypoint;

import java.io.File;

public class ParkourBeat extends JavaPlugin {

    public void onEnable() {
        Settings.load();
        ConfigurationSerialization.registerClass(Waypoint.class);

        LevelsManager levelsManager = new LevelsManager(new FileLevelSettingDAO(getDataFolder() + File.separator + "settings"));
        GameManager gameManager = new GameManager(levelsManager);
        LevelEditorsManager levelEditorsManager = new LevelEditorsManager(gameManager, levelsManager);

        PluginCommand tpToWorldCommand = getCommand("tptoworld");
        TpToWorldCommand command = new TpToWorldCommand(gameManager, levelsManager);
        tpToWorldCommand.setExecutor(command);
        tpToWorldCommand.setTabCompleter(command);

        getCommand("play").setExecutor(new PlayCommand(gameManager, levelsManager));
        getCommand("editor").setExecutor(new EditorCommand(levelEditorsManager, levelsManager));

        Bukkit.getPluginManager().registerEvents(new EventListener(gameManager, levelEditorsManager), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new ResourcePackListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new SprintListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new LevelEditorListener(levelEditorsManager), this);
    }

    public static JavaPlugin getPlugin() {
        return JavaPlugin.getPlugin(ParkourBeat.class);
    }

    public void onDisable() {

    }
}
