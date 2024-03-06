package ru.sortix.parkourbeat.levels;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class DirectionChecker {
    private final Direction direction;

    public DirectionChecker(Direction direction) {
        this.direction = direction;
    }

    public boolean isCorrectDirection(Location behind, Location forward) {
        switch (direction) {
            case NEGATIVE_X:
                return behind.getX() > forward.getX();
            case POSITIVE_X:
                return behind.getX() < forward.getX();
            case NEGATIVE_Z:
                return behind.getZ() > forward.getZ();
            case POSITIVE_Z:
                return behind.getZ() < forward.getZ();
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public boolean isAheadDirection(@NonNull Location location, double coordinate) {
        switch (direction) {
            case NEGATIVE_X:
                return location.getX() < coordinate;
            case POSITIVE_X:
                return location.getX() > coordinate;
            case NEGATIVE_Z:
                return location.getZ() < coordinate;
            case POSITIVE_Z:
                return location.getZ() > coordinate;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public void add(Vector vector, double value) {
        switch (direction) {
            case NEGATIVE_X:
                vector.setX(vector.getX() - value);
                break;
            case POSITIVE_X:
                vector.setX(vector.getX() + value);
                break;
            case NEGATIVE_Z:
                vector.setZ(vector.getZ() - value);
                break;
            case POSITIVE_Z:
                vector.setZ(vector.getZ() + value);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public void subtract(Vector vector, double value) {
        add(vector, -value);
    }

    public double getCoordinate(Location location) {
        return getCoordinate(location.toVector());
    }

    public double getCoordinate(Vector vector) {
        switch (direction) {
            case NEGATIVE_X:
            case POSITIVE_X:
                return vector.getX();
            case NEGATIVE_Z:
            case POSITIVE_Z:
                return vector.getZ();
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public boolean isNegative() {
        return direction == Direction.NEGATIVE_X || direction == Direction.NEGATIVE_Z;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Z,
        NEGATIVE_Z
    }
}
