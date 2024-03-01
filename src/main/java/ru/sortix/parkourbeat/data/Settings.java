package ru.sortix.parkourbeat.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.WorldsManager;

@UtilityClass
public class Settings {
    @Getter private Location lobbySpawn;
    @Getter private Location defaultWorldSpawn;
    private boolean isLoaded = false;

    public void load(@NonNull ParkourBeat plugin, @NonNull WorldsManager worldsManager) {
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
            String worldName = spawnSection.getString("world");
            WorldCreator worldCreator = newWorldCreator(worldName);
            spawnWorld =
                    worldsManager
                            .createWorldFromDefaultContainer(
                                    worldCreator, worldsManager.getCurrentThreadExecutor())
                            .join();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load lobby world", e);
        }

        lobbySpawn.setWorld(spawnWorld);
        defaultWorldSpawn = getLocation(config.getConfigurationSection("default_world"));
        isLoaded = true;
    }

    @NonNull private WorldCreator newWorldCreator(@NonNull String worldName) {
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.seed(0L);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);
        return worldCreator;
    }

    private static Location getLocation(ConfigurationSection config) {
        double x = config.getDouble("x", 0);
        double y = config.getDouble("y", 0);
        double z = config.getDouble("z", 0);
        float yaw = (float) config.getDouble("yaw", 0);
        float pitch = (float) config.getDouble("pitch", 0);
        return new Location(null, x, y, z, yaw, pitch);
    }
}
