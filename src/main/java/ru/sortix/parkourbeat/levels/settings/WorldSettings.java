package ru.sortix.parkourbeat.levels.settings;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.location.Region;

public class WorldSettings {

    private final World world;
    private final Location spawn;
    private final Region startRegion, gameRegion, finishRegion;

    public WorldSettings(World world, Vector spawn, Region startRegion, Region gameRegion, Region finishRegion) {
        this.world = world;
        this.spawn = spawn.toLocation(world);
        this.startRegion = startRegion;
        this.gameRegion = gameRegion;
        this.finishRegion = finishRegion;
    }

    public World getWorld() {
        return world;
    }

    public Location getSpawn() {
        return spawn;
    }

    public Region getStartRegion() {
        return startRegion;
    }

    public Region getGameRegion() {
        return gameRegion;
    }

    public Region getFinishRegion() {
        return finishRegion;
    }
}
