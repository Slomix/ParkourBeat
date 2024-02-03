package parkourbeat.levels.settings;

import org.bukkit.World;
import parkourbeat.levels.settings.GameSettings;
import parkourbeat.levels.settings.WorldSettings;

public class LevelSettings {

    private final WorldSettings worldSettings;
    private final GameSettings gameSettings;

    public LevelSettings(WorldSettings worldSettings, GameSettings gameSettings) {
        this.worldSettings = worldSettings;
        this.gameSettings = gameSettings;
    }

    public WorldSettings getWorldSettings() {
        return worldSettings;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public static LevelSettings create(World world) {
        return new LevelSettings(new WorldSettings(world, null, null, null, null),
                new GameSettings(null, null, null));
    }

}
