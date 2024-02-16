package ru.sortix.parkourbeat.levels;

import org.bukkit.World;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public class Level {

  private final World world;
  private final String name;
  private final LevelSettings levelSettings;
  private boolean isEditing = false;

  public Level(String name, World world, LevelSettings levelSettings) {
    this.name = name;
    this.world = world;
    this.levelSettings = levelSettings;
  }

  public World getWorld() {
    return world;
  }

  public String getName() {
    return name;
  }

  public LevelSettings getLevelSettings() {
    return levelSettings;
  }

  public boolean isEditing() {
    return isEditing;
  }

  public void setEditing(boolean isEditing) {
    this.isEditing = isEditing;
  }
}
