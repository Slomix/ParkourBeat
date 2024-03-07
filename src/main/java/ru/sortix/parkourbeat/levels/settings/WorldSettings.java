package ru.sortix.parkourbeat.levels.settings;

import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.location.Waypoint;

@Getter
public class WorldSettings {

    private final World world;
    private final List<Waypoint> waypoints;
    private final int minWorldHeight;

    @Setter
    private Location spawn;

    @Setter
    private Vector startBorder;

    @Setter
    private Vector finishBorder;

    public WorldSettings(
            @NonNull World world,
            @NonNull Location spawn,
            @NonNull Vector startRegion,
            @NonNull Vector finishRegion,
            @NonNull List<Waypoint> waypoints) {
        this.world = world;
        this.spawn = spawn;
        this.startBorder = startRegion;
        this.finishBorder = finishRegion;
        this.waypoints = waypoints;
        this.minWorldHeight = this.findMinWorldHeight();

        if (waypoints.isEmpty()) {
            waypoints.add(new Waypoint(startBorder.toLocation(world), Color.LIME, 0));
            waypoints.add(new Waypoint(finishBorder.toLocation(world), Color.LIME, 0));
        }

        this.world.setSpawnLocation(spawn);
    }

    private int findMinWorldHeight() {
        if (this.waypoints.isEmpty()) {
            return 0;
        }

        int minWorldHeight = Integer.MAX_VALUE;
        for (Waypoint waypoint : this.waypoints) {
            minWorldHeight = Math.min(minWorldHeight, waypoint.getLocation().getBlockY());
        }
        return minWorldHeight;
    }

    public DirectionChecker.Direction getDirection() {
        if (startBorder == null || finishBorder == null) {
            return null;
        }
        if (Math.abs(startBorder.getX() - finishBorder.getX()) > Math.abs(startBorder.getZ() - finishBorder.getZ())) {
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

    public void sortWaypoints(@NonNull DirectionChecker directionChecker) {
        Comparator<Waypoint> comparator =
                Comparator.comparingDouble(waypoint -> directionChecker.getCoordinate(waypoint.getLocation()));

        if (directionChecker.isNegative()) comparator = comparator.reversed();

        this.waypoints.sort(comparator);

        Location prevLocation = null;
        for (Waypoint waypoint : this.waypoints) {
            if (waypoint.getLocation().equals(prevLocation)) {
                System.out.println("Duplicate point: " + prevLocation);
            }
            prevLocation = waypoint.getLocation();
        }
    }

    @NonNull public Location getStartBorderLoc() {
        return this.startBorder.toLocation(this.world);
    }

    @NonNull public Location getFinishBorderLoc() {
        return this.finishBorder.toLocation(this.world);
    }

    public boolean isWorldEmpty() {
        return this.world.getPlayers().isEmpty();
    }

    public void updateEndWaypoints(DirectionChecker directionChecker) {
        Waypoint startWaypoint = this.waypoints.get(0);
        Waypoint endWaypoint = this.waypoints.get(this.waypoints.size() - 1);
        startWaypoint.setLocation(startBorder.toLocation(this.world));
        endWaypoint.setLocation(finishBorder.toLocation(this.world));
        double startCoordinate = directionChecker.getCoordinate(startWaypoint.getLocation());
        double endCoordinate = directionChecker.getCoordinate(endWaypoint.getLocation());
        waypoints.removeIf(waypoint -> {
            if (directionChecker.isNegative()) {
                return directionChecker.getCoordinate(waypoint.getLocation()) > startCoordinate;
            } else {
                return directionChecker.getCoordinate(waypoint.getLocation()) < startCoordinate;
            }
        });
        waypoints.removeIf(waypoint -> {
            if (directionChecker.isNegative()) {
                return directionChecker.getCoordinate(waypoint.getLocation()) < endCoordinate;
            } else {
                return directionChecker.getCoordinate(waypoint.getLocation()) > endCoordinate;
            }
        });
    }
}
