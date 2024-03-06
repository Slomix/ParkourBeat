package ru.sortix.parkourbeat.levels.dao.files;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Waypoint;

public class WorldSettingsDAO {
    public void set(WorldSettings worldSettings, FileConfiguration config) {
        Location spawn = worldSettings.getSpawn().clone();
        spawn.setWorld(null);
        config.set("spawn", spawn);
        config.set("start_border", worldSettings.getStartBorder());
        config.set("finish_border", worldSettings.getFinishBorder());
        config.set(
                "waypoints",
                worldSettings.getWaypoints().stream()
                        .map(
                                waypoint -> {
                                    Location loc = waypoint.getLocation();
                                    return new Waypoint(
                                            new Location(null, loc.getX(), loc.getY(), loc.getZ()),
                                            waypoint.getColor(),
                                            waypoint.getHeight());
                                })
                        .collect(Collectors.toList()));
    }

    @NonNull public WorldSettings load(@NonNull FileConfiguration config, @NonNull World world) {
        Location spawn = config.getSerializable("spawn", Location.class);
        if (spawn == null) {
            throw new IllegalArgumentException("Object \"spawn\" not found");
        }
        spawn.setWorld(world);

        Vector startBorder = config.getVector("start_border");
        if (startBorder == null) {
            throw new IllegalArgumentException("Vector \"start_border\" not found");
        }
        Vector finishBorder = config.getVector("finish_border");
        if (finishBorder == null) {
            throw new IllegalArgumentException("Vector \"finish_border\" not found");
        }

        //noinspection unchecked
        List<Waypoint> particleSegment = (List<Waypoint>) config.getList("waypoints");
        if (particleSegment == null) {
            throw new IllegalArgumentException("List not found: \"waypoints\"");
        }
        for (Waypoint waypoint : particleSegment) {
            waypoint.getLocation().setWorld(world);
        }

        return new WorldSettings(world, spawn, startBorder, finishBorder, particleSegment);
    }
}
