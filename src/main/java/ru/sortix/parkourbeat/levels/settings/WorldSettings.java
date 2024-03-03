package ru.sortix.parkourbeat.levels.settings;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.location.Waypoint;

@Getter
public class WorldSettings {

    private final World world;
    private final ArrayList<Waypoint> waypoints;
    private final int minWorldHeight;
    @Setter private Location spawn;
    @Setter private Vector startBorder;
    @Setter private Vector finishBorder;

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
