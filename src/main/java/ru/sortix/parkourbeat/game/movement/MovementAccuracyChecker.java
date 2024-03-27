package ru.sortix.parkourbeat.game.movement;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.Waypoint;

import java.util.List;

public class MovementAccuracyChecker {

    private static final double MAX_ALLOW_OFFSET = 0.1;
    private final @NonNull List<Waypoint> waypoints;
    private final @NonNull DirectionChecker directionChecker;
    @Getter
    private double accuracy;
    private int currentSegment;
    private int totalSteps;
    private double totalOffset;

    public MovementAccuracyChecker(@NonNull List<Waypoint> waypoints, @NonNull DirectionChecker directionChecker) {
        this.waypoints = waypoints;
        this.directionChecker = directionChecker;
        this.reset();
    }

    public void onPlayerLocationChange(@NonNull Location newLocation) {
        if (this.currentSegment >= this.waypoints.size() - 1) {
            return;
        }
        Location previousLocation = null;
        if (this.currentSegment < this.waypoints.size() - 2) {
            previousLocation = this.waypoints.get(this.currentSegment + 1).getLocation();
            if (this.directionChecker.isCorrectDirection(previousLocation, newLocation)) {
                this.currentSegment++;
            } else {
                previousLocation = null;
            }
        }

        Location point1 = previousLocation != null
            ? previousLocation
            : this.waypoints.get(this.currentSegment).getLocation();
        Location point2 = this.waypoints.get(this.currentSegment + 1).getLocation();

        double distanceToLine = calculateDistanceToLine(newLocation, point1, point2);

        if (distanceToLine > MAX_ALLOW_OFFSET) {
            this.totalOffset += distanceToLine - MAX_ALLOW_OFFSET;
        }
        this.totalSteps++;

        double averageDeviation = this.totalOffset / this.totalSteps;

        this.accuracy = 1.0 / (1.0 + averageDeviation);
    }

    public void reset() {
        this.accuracy = 1;
        this.currentSegment = 0;
        this.totalSteps = 0;
        this.totalOffset = 0;
    }

    /**
     * Calculates the distance from a point to a line defined by two other points.
     *
     * @param point      the location of the point
     * @param linePoint1 the first location defining the line
     * @param linePoint2 the second location defining the line
     * @return the distance from the point to the line
     */
    private double calculateDistanceToLine(@NonNull Location point, @NonNull Location linePoint1, @NonNull Location linePoint2) {
        Vector lineVector = linePoint2.toVector().subtract(linePoint1.toVector());
        Vector pointVector = point.toVector().subtract(linePoint1.toVector());

        lineVector.setY(0);
        pointVector.setY(0);

        double dotProduct = lineVector.dot(pointVector);
        Vector projection = lineVector.multiply(dotProduct / lineVector.lengthSquared());

        Vector perpendicular = pointVector.subtract(projection);

        return perpendicular.length();
    }
}
