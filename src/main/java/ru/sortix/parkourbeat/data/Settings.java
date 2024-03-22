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
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.utils.ConfigUtils;
import ru.sortix.parkourbeat.world.Cuboid;
import ru.sortix.parkourbeat.world.WorldsManager;

@UtilityClass
public class Settings {
    private boolean isLoaded = false;

    // lobby options
    private @Getter Location lobbySpawn;

    // level fixed options
    private @Getter Cuboid levelFixedEditableArea;

    // level default options
    private @Getter DirectionChecker.Direction levelDefaultDirection;
    private @Getter Vector levelDefaultStartPoint;
    private @Getter Vector levelDefaultFinishPoint;
    private @Getter Location levelDefaultSpawn;

    public void load(@NonNull ParkourBeat plugin, @NonNull WorldsManager worldsManager) {
        if (isLoaded) throw new IllegalStateException("Settings already loaded");

        plugin.saveDefaultConfig();

        ConfigurationSection rootConfig = plugin.getConfig();

        ConfigurationSection lobbyConfig = rootConfig.getConfigurationSection("lobby");
        if (lobbyConfig == null) {
            throw new IllegalArgumentException("Section \"default_level\" not found");
        }
        lobbySpawn = getLocation(lobbyConfig, "spawn_pos", worldsManager, true);
        lobbySpawn.getWorld().setSpawnLocation(lobbySpawn);

        ConfigurationSection allLevelsConfig = rootConfig.getConfigurationSection("all_levels");
        if (allLevelsConfig == null) {
            throw new IllegalArgumentException("Section \"all_levels\" not found");
        }
        levelFixedEditableArea = new Cuboid(
                ConfigUtils.parsePointXYZ(allLevelsConfig.getString("min_editable_point")),
                ConfigUtils.parsePointXYZ(allLevelsConfig.getString("max_editable_point")));

        ConfigurationSection defaultLevelConfig = rootConfig.getConfigurationSection("default_level");
        if (defaultLevelConfig == null) {
            throw new IllegalArgumentException("Section \"default_level\" not found");
        }

        levelDefaultDirection = ConfigUtils.parseDirection(defaultLevelConfig, "direction");
        levelDefaultStartPoint = ConfigUtils.parsePointXYZ(defaultLevelConfig.getString("start_point"));
        levelDefaultFinishPoint = ConfigUtils.parsePointXYZ(defaultLevelConfig.getString("finish_point"));
        levelDefaultSpawn = getLocation(defaultLevelConfig, "spawn_pos", null, true);

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

    @NonNull private Location getLocation(
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
            if (worldName == null) {
                throw new IllegalArgumentException("World name not provided");
            }
            try {
                WorldCreator worldCreator = newWorldCreator(worldName);
                world = worldsManager
                        .createWorldFromDefaultContainer(worldCreator, worldsManager.getCurrentThreadExecutor())
                        .join();
                if (world == null) {
                    throw new IllegalArgumentException("Unable to create bukkit world from default container");
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to load world from config", e);
            }
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
}
