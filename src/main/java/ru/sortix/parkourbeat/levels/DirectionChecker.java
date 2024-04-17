package ru.sortix.parkourbeat.levels;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
@Getter
public class DirectionChecker {
    private final @NonNull Direction direction;

    public boolean isCorrectDirection(@NonNull Location behind, @NonNull Location to) {
        switch (direction) {
            case NEGATIVE_X:
                return behind.getX() >= to.getX();
            case POSITIVE_X:
                return behind.getX() <= to.getX();
            case NEGATIVE_Z:
                return behind.getZ() >= to.getZ();
            case POSITIVE_Z:
                return behind.getZ() <= to.getZ();
            default:
                throw new IllegalArgumentException("Invalid direction: " + this.direction);
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
                throw new IllegalArgumentException("Invalid direction: " + this.direction);
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

    public double getCoordinateWithSign(Location location) {
        return getCoordinateWithSign(location.toVector());
    }

    public double getCoordinateWithSign(Vector vector) {
        return (isNegative() ? -1 : 1) * getCoordinate(vector);
    }

    public boolean isNegative() {
        return this.direction == Direction.NEGATIVE_X || this.direction == Direction.NEGATIVE_Z;
    }

    public enum Direction {
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Z,
        NEGATIVE_Z
    }
}
