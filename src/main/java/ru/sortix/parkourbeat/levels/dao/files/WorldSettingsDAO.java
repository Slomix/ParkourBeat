package ru.sortix.parkourbeat.levels.dao.files;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Waypoint;
import ru.sortix.parkourbeat.location.Region;

import java.util.ArrayList;

public class WorldSettingsDAO {

    public void set(WorldSettings worldSettings, FileConfiguration config) {
        config.set("spawn", worldSettings.getSpawn());
        config.set("start_region", worldSettings.getStartRegion());
        config.set("game_region", worldSettings.getGameRegion());
        config.set("finish_region", worldSettings.getFinishRegion());
        config.set("waypoints", worldSettings.getParticlePoints());
    }

    public WorldSettings load(FileConfiguration config, World world) {
        Location spawn = config.getSerializable("spawn", Location.class);
        Region startRegion = config.getSerializable("start_region", Region.class);
        Region gameRegion = config.getSerializable("game_region", Region.class);
        Region finishRegion = config.getSerializable("finish_region", Region.class);
        ArrayList<Waypoint> particleSegment = (ArrayList<Waypoint>) config.getList("waypoints");

        return new WorldSettings(world, spawn, startRegion, gameRegion, finishRegion, particleSegment);
    }

}
