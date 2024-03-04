package ru.sortix.parkourbeat.levels.dao.files;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Waypoint;

@RequiredArgsConstructor
public class WorldSettingsDAO {
    private final Logger logger;

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

    public WorldSettings load(FileConfiguration config, World world) {
        Location spawn = config.getSerializable("spawn", Location.class);
        spawn.setWorld(world);
        Vector startBorder = config.getVector("start_border");
        Vector finishBorder = config.getVector("finish_border");
        if (startBorder == null || finishBorder == null) {
            this.logger.warning("Unable to load border positions of world " + world.getName());
        }
        if (startBorder == null) {
            startBorder = Settings.getStartBorder();
        }
        if (finishBorder == null) {
            finishBorder = Settings.getFinishBorder();
        }
        List<Waypoint> particleSegment = (List<Waypoint>) config.getList("waypoints");
        for (Waypoint waypoint : particleSegment) {
            waypoint.getLocation().setWorld(world);
        }

        return new WorldSettings(world, spawn, startBorder, finishBorder, particleSegment);
    }
}
