package parkourbeat;

import parkourbeat.commands.PlayCommand;
import parkourbeat.commands.TpToWorldCommand;
import parkourbeat.commands.TpToWorldTabComplete;
import parkourbeat.data.Settings;
import parkourbeat.levels.settings.GameSettings;
import parkourbeat.listeners.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import parkourbeat.levels.dao.FileLevelSettingDAO;
import parkourbeat.levels.LevelsManager;

import java.util.Set;

public class ParkourBeat extends JavaPlugin {
    private static ParkourBeat plugin;

    public static ParkourBeat getInstance() {
        return plugin;
    }

    private LevelsManager levelsManager;

    public void onEnable() {
        plugin = this;
        Settings.load();
        levelsManager = new LevelsManager(new FileLevelSettingDAO("settings"));
        PluginCommand tpToWorldCommand = getCommand("tptoworld");
        tpToWorldCommand.setExecutor(new TpToWorldCommand());
        tpToWorldCommand.setTabCompleter(new TpToWorldTabComplete());
        getCommand("play").setExecutor(new PlayCommand());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }

    public void onDisable() {

    }

    public static LevelsManager getLevelsManager() {
        return plugin.levelsManager;
    }
}
