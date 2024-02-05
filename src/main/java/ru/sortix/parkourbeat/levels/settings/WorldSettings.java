package ru.sortix.parkourbeat.levels.settings;

import org.bukkit.Location;
import org.bukkit.World;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.location.Waypoint;
import ru.sortix.parkourbeat.location.Region;

import java.util.*;

public class WorldSettings {

    private final World world;
    private final Location spawn;
    private final Region startRegion, gameRegion, finishRegion;
    private final ArrayList<Waypoint> waypoint;

    public WorldSettings(World world, Location spawn, Region startRegion, Region gameRegion, Region finishRegion, ArrayList<Waypoint> waypoint) {
        this.world = world;
        this.spawn = spawn;
        this.startRegion = startRegion;
        this.gameRegion = gameRegion;
        this.finishRegion = finishRegion;
        this.waypoint = waypoint;
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

    public ArrayList<Waypoint> getParticlePoints() {
        return waypoint;
    }

    public DirectionChecker.Direction getDirection() {
        if (startRegion.getCenter().getX() < finishRegion.getCenter().getX()) {
            return DirectionChecker.Direction.POSITIVE_X;
        } else if (startRegion.getCenter().getX() > finishRegion.getCenter().getX()) {
            return DirectionChecker.Direction.NEGATIVE_X;
        } else if (startRegion.getCenter().getZ() < finishRegion.getCenter().getZ()) {
            return DirectionChecker.Direction.POSITIVE_Z;
        } else if (startRegion.getCenter().getZ() > finishRegion.getCenter().getZ()) {
            return DirectionChecker.Direction.NEGATIVE_Z;
        }
        return null;
    }
}
