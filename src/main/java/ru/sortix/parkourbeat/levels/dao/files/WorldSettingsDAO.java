package ru.sortix.parkourbeat.levels.dao.files;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Waypoint;

public class WorldSettingsDAO {
    public void set(@NonNull WorldSettings worldSettings, @NonNull FileConfiguration config) {
        Location spawn = worldSettings.getSpawn().clone();
        spawn.setWorld(null);
        config.set("environment", worldSettings.getEnvironment().name());
        config.set("spawn", spawn);
        config.set("start_border", worldSettings.getStartBorder());
        config.set("finish_border", worldSettings.getFinishBorder());
        config.set("direction", worldSettings.getDirection().name());
        config.set(
                "waypoints",
                worldSettings.getWaypoints().stream()
                        .map(waypoint -> {
                            Location loc = waypoint.getLocation();
                            return new Waypoint(
                                    new Location(null, loc.getX(), loc.getY(), loc.getZ()),
                                    waypoint.getColor(),
                                    waypoint.getHeight());
                        })
                        .collect(Collectors.toList()));
    }

    @NonNull public WorldSettings load(@NonNull FileConfiguration config, @NonNull World world) {
        String environmentName = config.getString("environment");
        if (environmentName == null) {
            throw new IllegalArgumentException("String \"environment\" not found");
        }
        World.Environment environment = World.Environment.valueOf(environmentName);

        Location spawn = config.getSerializable("spawn", Location.class);
        if (spawn == null) {
            throw new IllegalArgumentException("Location \"spawn\" not found");
        }
        spawn.setWorld(world);

        DirectionChecker.Direction direction = DirectionChecker.Direction.valueOf(config.getString("direction"));

        //noinspection unchecked
        List<Waypoint> particleSegment = (List<Waypoint>) config.getList("waypoints");
        if (particleSegment == null) {
            throw new IllegalArgumentException("List of Waypoints \"waypoints\" not found");
        }
        for (Waypoint waypoint : particleSegment) {
            waypoint.getLocation().setWorld(world);
        }

        return new WorldSettings(world, environment, spawn, direction, particleSegment);
    }
}
