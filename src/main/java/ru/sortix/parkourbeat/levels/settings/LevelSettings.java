package ru.sortix.parkourbeat.levels.settings;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.ParticleController;

public class LevelSettings {

  private final WorldSettings worldSettings;
  private final GameSettings gameSettings;
  private final ParticleController particleController;
  private DirectionChecker directionChecker;

  public LevelSettings(WorldSettings worldSettings, GameSettings gameSettings) {
    this.worldSettings = worldSettings;
    this.gameSettings = gameSettings;
    this.directionChecker = new DirectionChecker(worldSettings.getDirection());
    this.particleController = new ParticleController(directionChecker);
  }

  public static LevelSettings create(World world, String owner) {
    Location defaultSpawn = Settings.getDefaultWorldSpawn().clone();
    defaultSpawn.setWorld(world);
    return new LevelSettings(
        new WorldSettings(world, defaultSpawn, null, null, new ArrayList<>()),
        new GameSettings(null, null, owner));
  }

  public WorldSettings getWorldSettings() {
    return worldSettings;
  }

  public GameSettings getGameSettings() {
    return gameSettings;
  }

  public ParticleController getParticleController() {
    return particleController;
  }

  public DirectionChecker getDirectionChecker() {
    return directionChecker;
  }

  public void updateDirectionChecker() {
    directionChecker = new DirectionChecker(worldSettings.getDirection());
    particleController.setDirectionChecker(directionChecker);
  }
}
