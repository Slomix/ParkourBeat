package ru.sortix.parkourbeat.levels.settings;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.item.editor.type.EditTrackPointsItem;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.Waypoint;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class WorldSettings {
    private final @NonNull World.Environment environment;
    private final @NonNull List<Waypoint> waypoints;
    private final int minWorldHeight; // TODO Update dynamically
    private final @NonNull DirectionChecker.Direction direction;

    @Setter
    private @NonNull Location spawn;

    @Setter
    private @NonNull Vector startWaypoint;

    @Setter
    private @NonNull Vector finishWaypoint;

    public WorldSettings(
        @NonNull World.Environment environment,
        @NonNull DirectionChecker.Direction direction,
        @NonNull Location spawn,
        @NonNull List<Waypoint> waypoints
    ) {

        this.environment = environment;
        this.spawn = spawn;
        this.waypoints = waypoints;
        this.direction = direction;
        this.minWorldHeight = this.findMinWorldHeight();

        if (waypoints.size() < 2) {
            throw new IllegalArgumentException("Unable to find start end finish points");
        }
        this.startWaypoint = waypoints.get(0).getLocation().toVector();
        this.finishWaypoint = waypoints.get(waypoints.size() - 1).getLocation().toVector();
    }

    public void addStartAndFinishPoints(@NonNull World world) {
        this.waypoints.add(new Waypoint(
            Settings.getLevelDefaultSettings().getStartWaypoint().toLocation(world),
            0, EditTrackPointsItem.DEFAULT_PARTICLES_COLOR));
        this.waypoints.add(new Waypoint(
            Settings.getLevelDefaultSettings().getFinishWaypoint().toLocation(world),
            0, EditTrackPointsItem.DEFAULT_PARTICLES_COLOR));
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
        this.startWaypoint = this.waypoints.get(0).getLocation().toVector();
        this.finishWaypoint = this.waypoints.get(this.waypoints.size() - 1).getLocation().toVector();
    }

    @NonNull
    public WorldSettings setWorld(@NonNull World.Environment environment, @Nullable World world) {
        Location spawn = this.getSpawn().clone();
        spawn.setWorld(world);

        DirectionChecker.Direction direction = this.getDirection();

        List<Waypoint> waypoints = new ArrayList<>(this.getWaypoints());
        for (Waypoint waypoint : waypoints) {
            waypoint.getLocation().setWorld(world);
        }

        return new WorldSettings(environment, direction, spawn, waypoints);
    }
}
