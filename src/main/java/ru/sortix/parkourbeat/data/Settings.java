package ru.sortix.parkourbeat.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
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
		lobbySpawn = getLocation(config.getConfigurationSection("lobby"));
		lobbySpawn.setWorld(Bukkit.getWorld(config.getString("lobby.world")));
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
