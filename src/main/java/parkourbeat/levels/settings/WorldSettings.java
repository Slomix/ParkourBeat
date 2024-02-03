package parkourbeat.levels.settings;

import org.bukkit.Location;
import org.bukkit.World;
import parkourbeat.location.Position;
import parkourbeat.location.Region;

public class WorldSettings {

    private final World world;
    private final Location spawn;
    private final Region startRegion, gameRegion, finishRegion;

    public WorldSettings(World world, Position spawn, Region startRegion, Region gameRegion, Region finishRegion) {
        this.world = world;
        this.spawn = spawn.getInWorld(world);
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
