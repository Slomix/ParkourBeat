package ru.sortix.parkourbeat.levels.settings;

import java.util.ArrayList;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.ParticleController;

@Getter
public class LevelSettings {

    private final WorldSettings worldSettings;
    private final GameSettings gameSettings;
    private final ParticleController particleController;
    private DirectionChecker directionChecker;

    public LevelSettings(@NonNull WorldSettings worldSettings, @NonNull GameSettings gameSettings) {
        this.worldSettings = worldSettings;
        this.gameSettings = gameSettings;
        this.directionChecker = new DirectionChecker(worldSettings.getDirection());
        this.particleController =
                new ParticleController(ParkourBeat.getPlugin(), worldSettings.getWorld(), this.directionChecker);

        // optional check is list sorted
        this.worldSettings.sortWaypoints(this.directionChecker);
    }

    public static LevelSettings create(
            @NonNull UUID levelId,
            @NonNull String levelName,
            @NonNull World world,
            @NonNull UUID ownerId,
            @NonNull String ownerName) {
        Location defaultSpawn = Settings.getDefaultWorldSpawn().clone();
        defaultSpawn.setWorld(world);
        return new LevelSettings(
                new WorldSettings(world, defaultSpawn, Settings.getDirection(), new ArrayList<>()),
                new GameSettings(levelId, levelName, ownerId, ownerName));
    }

    public void updateDirectionChecker() {
        directionChecker = new DirectionChecker(worldSettings.getDirection());
        particleController.setDirectionChecker(directionChecker);
    }

    public void updateParticleLocations() {
        this.getParticleController()
                .loadParticleLocations(this.getWorldSettings().getWaypoints());
    }
}
