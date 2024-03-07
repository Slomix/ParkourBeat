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
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.location.Waypoint;

@Getter
public class WorldSettings {

    private final World world;
    private final List<Waypoint> waypoints;
    private final int minWorldHeight;
    private final DirectionChecker.Direction direction;

    @Setter
    private Location spawn;

    @Setter
    private Vector startBorder;

    @Setter
    private Vector finishBorder;

    public WorldSettings(
            @NonNull World world,
            @NonNull Location spawn,
            @NonNull DirectionChecker.Direction direction,
            @NonNull List<Waypoint> waypoints) {
        this.world = world;
        this.spawn = spawn;
        this.waypoints = waypoints;
        this.direction = direction;
        this.minWorldHeight = this.findMinWorldHeight();

        if (waypoints.isEmpty()) {
            waypoints.add(new Waypoint(Settings.getStartBorder().toLocation(world), Color.LIME, 0));
            waypoints.add(new Waypoint(Settings.getFinishBorder().toLocation(world), Color.LIME, 0));
        }

        this.startBorder = waypoints.get(0).getLocation().toVector();
        this.finishBorder = waypoints.get(waypoints.size() - 1).getLocation().toVector();

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

    public void updateBorders() {
        this.startBorder = waypoints.get(0).getLocation().toVector();
        this.finishBorder = waypoints.get(waypoints.size() - 1).getLocation().toVector();
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
}
