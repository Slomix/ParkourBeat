package ru.sortix.parkourbeat.levels.dao.files;

import java.util.List;
import java.util.stream.Collectors;
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
                                    return new Location(null, loc.getX(), loc.getY(), loc.getZ());
                                })
                        .collect(Collectors.toList()));
    }

    public WorldSettings load(FileConfiguration config, World world) {
        Location spawn = config.getSerializable("spawn", Location.class);
        spawn.setWorld(world);
        Vector startBorder = config.getVector("start_border");
        Vector finishBorder = config.getVector("finish_border");
        List<Waypoint> particleSegment = (List<Waypoint>) config.getList("waypoints");
        for (Waypoint waypoint : particleSegment) {
            waypoint.getLocation().setWorld(world);
        }

        return new WorldSettings(world, spawn, startBorder, finishBorder, particleSegment);
    }
}
