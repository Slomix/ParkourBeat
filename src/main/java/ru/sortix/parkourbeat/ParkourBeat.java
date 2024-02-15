package ru.sortix.parkourbeat;

import me.bomb.amusic.AMusic;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import ru.sortix.parkourbeat.commands.*;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.data.Songs;
import ru.sortix.parkourbeat.editor.LevelEditorListener;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.editor.menu.SongMenuListener;
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

    private static Songs songs;

    public void onEnable() {
        Settings.load();
        songs = new Songs(getPlugin(AMusic.class).getDataFolder().toPath().resolve("Music"));

        ConfigurationSerialization.registerClass(Waypoint.class);
        LevelsManager levelsManager = new LevelsManager(this, new FileLevelSettingDAO(getDataFolder() + File.separator + "settings"));
        GameManager gameManager = new GameManager(levelsManager);
        LevelEditorsManager levelEditorsManager = new LevelEditorsManager(gameManager, levelsManager);

        registerCommand("tptoworld", new TpToWorldCommand(levelEditorsManager, gameManager, levelsManager));
        registerCommand("play", new PlayCommand(gameManager, levelsManager));
        registerCommand("edit", new EditCommand(levelEditorsManager, levelsManager, gameManager));
        registerCommand("create", new CreateCommand(levelEditorsManager, levelsManager, gameManager));
        registerCommand("delete", new DeleteCommand(levelEditorsManager, levelsManager, gameManager));
        registerCommand("song", new SongCommand(levelEditorsManager), false);
        registerCommand("color", new ColorCommand(levelEditorsManager), false);

        Bukkit.getPluginManager().registerEvents(new EventListener(gameManager, levelEditorsManager), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new ResourcePackListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new SprintListener(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new LevelEditorListener(levelEditorsManager), this);
        Bukkit.getPluginManager().registerEvents(new SongMenuListener(), this);
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        registerCommand(commandName, executor, true);
    }

    public void registerCommand(String commandName, CommandExecutor executor, boolean completer) {
        PluginCommand command = getCommand(commandName);
        command.setExecutor(executor);
        if (completer)
            command.setTabCompleter((TabCompleter) executor);
    }

    public static JavaPlugin getPlugin() {
        return JavaPlugin.getPlugin(ParkourBeat.class);
    }

    public static Songs getSongs() {
        return songs;
    }

    public void onDisable() {

    }
}
