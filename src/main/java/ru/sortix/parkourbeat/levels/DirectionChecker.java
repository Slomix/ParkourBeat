package ru.sortix.parkourbeat.levels;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public record DirectionChecker(@NonNull DirectionChecker.Direction direction) {
    public boolean isCorrectDirection(@NonNull Location behind, @NonNull Location to) {
        return switch (direction) {
            case NEGATIVE_X -> behind.getX() >= to.getX();
            case POSITIVE_X -> behind.getX() <= to.getX();
            case NEGATIVE_Z -> behind.getZ() >= to.getZ();
            case POSITIVE_Z -> behind.getZ() <= to.getZ();
        };
    }

    public boolean isAheadDirection(@NonNull Location location, double coordinate) {
        return switch (direction) {
            case NEGATIVE_X -> location.getX() < coordinate;
            case POSITIVE_X -> location.getX() > coordinate;
            case NEGATIVE_Z -> location.getZ() < coordinate;
            case POSITIVE_Z -> location.getZ() > coordinate;
        };
    }

    public void add(@NonNull Vector vector, double value) {
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
                throw new IllegalArgumentException("Invalid direction: " + this.direction);
        }
    }

    public double getCoordinate(@NonNull Location location) {
        return getCoordinate(location.toVector());
    }

    public double getCoordinate(@NonNull Vector vector) {
        return switch (this.direction) {
            case NEGATIVE_X, POSITIVE_X -> vector.getX();
            case NEGATIVE_Z, POSITIVE_Z -> vector.getZ();
        };
    }

    public double getCoordinateWithSign(@NonNull Location location) {
        return getCoordinateWithSign(location.toVector());
    }

    public double getCoordinateWithSign(@NonNull Vector vector) {
        return (isNegative() ? -1 : 1) * getCoordinate(vector);
    }

    public boolean isNegative() {
        return switch (this.direction) {
            case NEGATIVE_X, NEGATIVE_Z -> true;
            case POSITIVE_X, POSITIVE_Z -> false;
        };
    }

    public enum Direction {
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Z,
        NEGATIVE_Z
    }
}
