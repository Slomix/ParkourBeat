package ru.sortix.parkourbeat.levels.settings;

import java.util.ArrayList;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.location.Waypoint;

public class WorldSettings {

    private final World world;
    private final ArrayList<Waypoint> waypoints;
    @Getter private final int minWorldHeight;
    private Location spawn;
    private Vector startBorder;
    private Vector finishBorder;

    public WorldSettings(
            World world,
            Location spawn,
            Vector startRegion,
            Vector finishRegion,
            ArrayList<Waypoint> waypoints) {
        this.world = world;
        this.spawn = spawn;
        this.startBorder = startRegion;
        this.finishBorder = finishRegion;
        this.waypoints = waypoints;

        if (this.waypoints.isEmpty()) {
            this.minWorldHeight = 0;
        } else {
            int minWorldHeight = Integer.MAX_VALUE;
            for (Waypoint waypoint : this.waypoints) {
                minWorldHeight = Math.min(minWorldHeight, waypoint.getLocation().getBlockY());
            }
            this.minWorldHeight = minWorldHeight - 1;
        }
    }

    public World getWorld() {
        return world;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Vector getStartBorder() {
        return startBorder;
    }

    public void setStartBorder(Vector startPoint) {
        this.startBorder = startPoint;
    }

    public Vector getFinishBorder() {
        return finishBorder;
    }

    public void setFinishBorder(Vector finishPoint) {
        this.finishBorder = finishPoint;
    }

    public DirectionChecker.Direction getDirection() {
        if (startBorder == null || finishBorder == null) {
            return null;
        }
        if (Math.abs(startBorder.getX() - finishBorder.getX())
                > Math.abs(startBorder.getZ() - finishBorder.getZ())) {
            if (startBorder.getX() < finishBorder.getX()) {
                return DirectionChecker.Direction.POSITIVE_X;
            } else {
                return DirectionChecker.Direction.NEGATIVE_X;
            }
        } else {
            if (startBorder.getZ() < finishBorder.getZ()) {
                return DirectionChecker.Direction.POSITIVE_Z;
            } else {
                return DirectionChecker.Direction.NEGATIVE_Z;
            }
        }
    }
}
