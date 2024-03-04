package ru.sortix.parkourbeat.levels.dao.files;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Waypoint;

public class WorldSettingsDAO {

    public void set(WorldSettings worldSettings, FileConfiguration config) {
        config.set("spawn", worldSettings.getSpawn());
        config.set("start_border", worldSettings.getStartBorder());
        config.set("finish_border", worldSettings.getFinishBorder());
        config.set("waypoints", worldSettings.getWaypoints());
    }

    public WorldSettings load(FileConfiguration config, World world) {
        Location spawn = config.getSerializable("spawn", Location.class);
        Vector startBorder = config.getVector("start_border");
        Vector finishBorder = config.getVector("finish_border");
        List<Waypoint> particleSegment = (List<Waypoint>) config.getList("waypoints");

        return new WorldSettings(world, spawn, startBorder, finishBorder, particleSegment);
    }
}
