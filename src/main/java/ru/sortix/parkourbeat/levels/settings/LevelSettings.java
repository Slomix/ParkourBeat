package ru.sortix.parkourbeat.levels.settings;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.ParticleController;

public class LevelSettings {

    private final WorldSettings worldSettings;
    private final GameSettings gameSettings;
    private final ParticleController particleController;
    private final DirectionChecker directionChecker;

    public LevelSettings(WorldSettings worldSettings, GameSettings gameSettings) {
        this.worldSettings = worldSettings;
        this.gameSettings = gameSettings;
        this.directionChecker = new DirectionChecker(worldSettings.getDirection());
        this.particleController = new ParticleController(directionChecker);
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

    public static LevelSettings create(World world) {
        return new LevelSettings(new WorldSettings(world, null, null, null, null, null),
                new GameSettings(null, null, null));
    }

    public DirectionChecker getDirectionChecker() {
        return directionChecker;
    }
}
