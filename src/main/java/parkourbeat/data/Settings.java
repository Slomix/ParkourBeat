package parkourbeat.data;

import org.bukkit.configuration.file.FileConfiguration;
import parkourbeat.ParkourBeat;
import parkourbeat.location.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import parkourbeat.levels.settings.GameSettings;

import java.io.*;
import java.util.*;

public class Settings {
    private static Location exitLocation;
    private static boolean isLoaded = false;

    public static void load() {
        if (isLoaded) {
            Bukkit.getLogger().warning("Settings already loaded!");
            return;
        }
        JavaPlugin plugin = ParkourBeat.getInstance();
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        String world = config.getString("spawn.world", "world");
        double x = config.getDouble("spawn.x", 0);
        double y = config.getDouble("spawn.y", 0);
        double z = config.getDouble("spawn.z", 0);
        float yaw = (float) config.getDouble("spawn.yaw", 0);
        float pitch = (float) config.getDouble("spawn.pitch", 0);
        exitLocation = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        isLoaded = true;
    }

    public static Location getExitLocation() {
        return exitLocation;
    }
}
