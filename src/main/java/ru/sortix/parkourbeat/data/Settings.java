package ru.sortix.parkourbeat.data;

import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.WorldsManager;

@UtilityClass
public class Settings {
    @Getter private Location lobbySpawn;
    @Getter private Vector startBorder;
    @Getter private Vector finishBorder;
    @Getter private Location defaultWorldSpawn;
    private boolean isLoaded = false;

    public void load(@NonNull ParkourBeat plugin, @NonNull WorldsManager worldsManager) {
        if (isLoaded) {
            plugin.getLogger().warning("Settings already loaded!");
            return;
        }

        plugin.saveDefaultConfig();

        ConfigurationSection rootConfig = plugin.getConfig();

        ConfigurationSection lobbyConfig = rootConfig.getConfigurationSection("lobby");
        lobbySpawn = getLocation(lobbyConfig, "spawn_pos", worldsManager, true);
        lobbySpawn.getWorld().setSpawnLocation(lobbySpawn);

        ConfigurationSection defaultLevelConfig = rootConfig.getConfigurationSection("default_level");
        startBorder = getVector(defaultLevelConfig, "start_border");
        finishBorder = getVector(defaultLevelConfig, "finish_border");
        defaultWorldSpawn = getLocation(defaultLevelConfig, "spawn_pos", null, true);

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

    @NonNull private static Location getLocation(
            @NonNull ConfigurationSection config,
            @NonNull String key,
            @Nullable WorldsManager worldsManager,
            boolean parseYawPitch) {
        ConfigurationSection section = config.getConfigurationSection(key);
        double x = section.getDouble("x", 0);
        double y = section.getDouble("y", 0);
        double z = section.getDouble("z", 0);
        float yaw = parseYawPitch ? (float) section.getDouble("yaw", 0) : 0f;
        float pitch = parseYawPitch ? (float) section.getDouble("pitch", 0) : 0f;

        World world;
        if (worldsManager == null) {
            world = null;
        } else {
            String worldName = section.getString("world");
            try {
                WorldCreator worldCreator = newWorldCreator(worldName);
                world =
                        worldsManager
                                .createWorldFromDefaultContainer(
                                        worldCreator, worldsManager.getCurrentThreadExecutor())
                                .join();
            } catch (Exception e) {
                throw new RuntimeException("Unable to load lobby world", e);
            }
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    @NonNull private static Vector getVector(@NonNull ConfigurationSection section, @NonNull String key) {
        String vectorString = section.getString(key);
        String[] args = vectorString.split(" ");
        if (args.length != 3) {
            throw new IllegalArgumentException("Wrong vector: " + vectorString);
        }
        return new Vector(
                Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
    }
}
