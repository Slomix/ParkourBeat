package ru.sortix.parkourbeat.data;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.ParkourBeat;

public class Settings {
    private static Location lobbySpawn;
    private static Location defaultWorldSpawn;
    private static boolean isLoaded = false;

    public static void load() {
        JavaPlugin plugin = ParkourBeat.getPlugin();
        if (isLoaded) {
            plugin.getLogger().warning("Settings already loaded!");
            return;
        }
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection spawnSection = config.getConfigurationSection("lobby");
        lobbySpawn = getLocation(spawnSection);

        World spawnWorld;
        try {
            spawnWorld = getOrLoadWord(plugin.getServer(), spawnSection.getString("world"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load lobby world", e);
        }

        lobbySpawn.setWorld(spawnWorld);
        defaultWorldSpawn = getLocation(config.getConfigurationSection("default_world"));
        isLoaded = true;
    }

    @NonNull private static World getOrLoadWord(@NonNull Server server, @Nullable String worldName) {
        if (worldName == null) {
            throw new IllegalArgumentException("World name not found");
        }

        World result = server.getWorld(worldName);
        if (result != null) return result;

        File worldDir = new File(server.getWorldContainer(), worldName);
        if (!worldDir.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: " + worldDir.getAbsolutePath());
        }

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.seed(0L);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);

        result = server.createWorld(worldCreator);
        if (result != null) return result;

        throw new UnsupportedOperationException(
                "Unable to create world \""
                        + worldName
                        + "\" from directory "
                        + worldDir.getAbsolutePath());
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
