package ru.sortix.parkourbeat.game.movement;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.location.Waypoint;

public class MovementAccuracyChecker {

    private final List<Waypoint> waypoints;
    private final DirectionChecker directionChecker;
    private double accuracy;
    private int currentSegment;
    private int totalSteps;
    private double totalOffset;

    private static final double MAX_ALLOW_OFFSET = 0.1;

    public MovementAccuracyChecker(List<Waypoint> waypoints, DirectionChecker directionChecker) {
        this.waypoints = waypoints;
        this.directionChecker = directionChecker;
        reset();
    }

    public void onPlayerLocationChange(Location newLocation) {
        if (currentSegment >= waypoints.size() - 1) {
            return;
        }
        Location previousLocation = null;
        if (currentSegment < waypoints.size() - 2) {
            previousLocation = waypoints.get(currentSegment + 1).getLocation();
            if (directionChecker.isCorrectDirection(previousLocation, newLocation)) {
                currentSegment++;
            } else {
                previousLocation = null;
            }
        }

        Location point1 = previousLocation != null
                ? previousLocation
                : waypoints.get(currentSegment).getLocation();
        Location point2 = waypoints.get(currentSegment + 1).getLocation();

        double distanceToLine = calculateDistanceToLine(newLocation, point1, point2);

        if (distanceToLine > MAX_ALLOW_OFFSET) {
            totalOffset += distanceToLine - MAX_ALLOW_OFFSET;
        }
        totalSteps++;

        double averageDeviation = totalOffset / totalSteps;

        accuracy = 1.0 / (1.0 + averageDeviation);
    }

    public void reset() {
        accuracy = 1;
        currentSegment = 0;
        totalSteps = 0;
        totalOffset = 0;
    }

    public double getAccuracy() {
        return accuracy;
    }

    /**
     * Calculates the distance from a point to a line defined by two other points.
     *
     * @param  point        the location of the point
     * @param  linePoint1   the first location defining the line
     * @param  linePoint2   the second location defining the line
     * @return the distance from the point to the line
     */
    private double calculateDistanceToLine(Location point, Location linePoint1, Location linePoint2) {
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
