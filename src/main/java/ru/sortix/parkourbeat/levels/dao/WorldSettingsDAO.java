package ru.sortix.parkourbeat.levels.dao;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Region;

public class WorldSettingsDAO {

    public void set(WorldSettings worldSettings, FileConfiguration config) {
        config.set("spawn", worldSettings.getSpawn());
        config.set("start_region", worldSettings.getStartRegion());
        config.set("game_region", worldSettings.getGameRegion());
        config.set("finish_region", worldSettings.getFinishRegion());
    }

    public WorldSettings load(FileConfiguration config, World world) {
        Vector spawn = config.getSerializable("spawn", Vector.class);
        Region startRegion = config.getSerializable("start_region", Region.class);
        Region gameRegion = config.getSerializable("game_region", Region.class);
        Region finishRegion = config.getSerializable("finish_region", Region.class);
        return new WorldSettings(world, spawn, startRegion, gameRegion, finishRegion);
    }

}
