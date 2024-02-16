package ru.sortix.parkourbeat.levels;

import org.bukkit.Location;

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

  public boolean isAheadDirection(Location location, double coordinate) {
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

  public double getCoordinate(Location location) {
    switch (direction) {
      case NEGATIVE_X:
      case POSITIVE_X:
        return location.getX();
      case NEGATIVE_Z:
      case POSITIVE_Z:
        return location.getZ();
      default:
        throw new IllegalArgumentException("Invalid direction: " + direction);
    }
  }

  public enum Direction {
    POSITIVE_X,
    NEGATIVE_X,
    POSITIVE_Z,
    NEGATIVE_Z
  }
}
