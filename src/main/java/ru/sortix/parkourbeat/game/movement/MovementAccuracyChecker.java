package ru.sortix.parkourbeat.game.movement;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.location.Waypoint;

import java.util.ArrayList;

public class MovementAccuracyChecker  {

    private final ArrayList<Waypoint> waypoint;
    private final DirectionChecker directionChecker;
    private double accuracy;
    private int currentSegment;
    private int totalSteps;
    private double totalDeviation;

    public MovementAccuracyChecker(ArrayList<Waypoint> waypoint, DirectionChecker directionChecker) {
        this.waypoint = waypoint;
        this.directionChecker = directionChecker;
        reset();
    }

    public void onPlayerLocationChange(Location newLocation) {
        if (currentSegment >= waypoint.size() - 1) {
            return;
        }
        Location previousLocation = null;
        if (currentSegment < waypoint.size() - 2) {
            previousLocation = waypoint.get(currentSegment + 1).getLocation();
            if (directionChecker.isCorrectDirection(previousLocation, newLocation)) {
                currentSegment++;
            } else {
                previousLocation = null;
            }
        }

        Location point1 = previousLocation != null ? previousLocation : waypoint.get(currentSegment).getLocation();
        Location point2 = waypoint.get(currentSegment + 1).getLocation();

        double distanceToLine = calculateDistanceToLine(newLocation, point1, point2);
        double segmentLength = point1.distance(point2);
        double accuracyChange = distanceToLine / segmentLength;

        totalDeviation += accuracyChange;
        totalSteps++;

        double averageDeviation = totalDeviation / totalSteps;

        accuracy = 1.0 / (1.0 + averageDeviation);

        // Ограничиваем точность диапазоном от 0 до 1
        accuracy = Math.max(0, Math.min(1, accuracy));
    }

    public void reset() {
        accuracy = 1;
        currentSegment = 0;
        totalSteps = 0;
        totalDeviation = 0;
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
     * @return             the distance from the point to the line
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
