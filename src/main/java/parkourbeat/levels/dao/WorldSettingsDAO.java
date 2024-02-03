package parkourbeat.levels.dao;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import parkourbeat.levels.settings.WorldSettings;
import parkourbeat.location.Position;
import parkourbeat.location.Region;

public class WorldSettingsDAO {

    public void set(WorldSettings worldSettings, FileConfiguration config) {
        config.set("spawn", worldSettings.getSpawn());
        config.set("start_region", worldSettings.getStartRegion());
        config.set("game_region", worldSettings.getGameRegion());
        config.set("finish_region", worldSettings.getFinishRegion());
    }

    public WorldSettings load(FileConfiguration config, World world) {
        Position spawn = config.getSerializable("spawn", Position.class);
        Region startRegion = config.getSerializable("start_region", Region.class);
        Region gameRegion = config.getSerializable("game_region", Region.class);
        Region finishRegion = config.getSerializable("finish_region", Region.class);
        return new WorldSettings(world, spawn, startRegion, gameRegion, finishRegion);
    }

}
