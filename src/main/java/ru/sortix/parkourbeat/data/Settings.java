package ru.sortix.parkourbeat.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.IllegalStateException;
import ru.sortix.parkourbeat.ParkourBeat;

public class Settings {
	private static Location lobbySpawn;
	private static Location defaultWorldSpawn;
	private static boolean isLoaded = false;

  public static void load() {
    if (isLoaded) {
      Bukkit.getLogger().warning("Settings already loaded!");
      return;
    }
    JavaPlugin plugin = ParkourBeat.getPlugin();
    plugin.saveDefaultConfig();
    FileConfiguration config = plugin.getConfig();
    ConfigurationSection spawnSection = config.getConfigurationSection("lobby");
    lobbySpawn = getLocation(spawnSection);
    World spawnWorld = Bukkit.getWorld(spawnSection.getString("world"));
    if (spawnWorld == null) {
        throw new IllegalStateException("No world for spawn with provided name is present!");
    }
    lobbySpawn.setWorld(spawnWorld);
    defaultWorldSpawn = getLocation(config.getConfigurationSection("default_world"));
    isLoaded = true;
  }

	private static Location getLocation(ConfigurationSection config) {
		double x = config.getDouble("x", 0);
		double y = config.getDouble("y", 0);
		double z = config.getDouble("z", 0);
		float yaw = (float) config.getDouble("yaw", 0);
		float pitch = (float) config.getDouble("pitch", 0);
		return new Location(null, x, y, z, yaw, pitch);
	}

	public static Location getLobbySpawn() {
		return lobbySpawn;
	}

	public static Location getDefaultWorldSpawn() {
		return defaultWorldSpawn;
	}
}
