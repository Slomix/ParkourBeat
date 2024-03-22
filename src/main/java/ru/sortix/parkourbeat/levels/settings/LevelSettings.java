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
    private final DirectionChecker directionChecker;

    public LevelSettings(@NonNull WorldSettings worldSettings, @NonNull GameSettings gameSettings) {
        this.worldSettings = worldSettings;
        this.gameSettings = gameSettings;
        this.directionChecker = new DirectionChecker(worldSettings.getDirection());
        this.particleController =
                new ParticleController(ParkourBeat.getPlugin(), worldSettings.getWorld(), this.directionChecker);

        // optional check is list sorted
        this.worldSettings.sortWaypoints(this.directionChecker);
    }

    @NonNull public static LevelSettings create(
            @NonNull World world,
            @NonNull World.Environment environment,
            @NonNull UUID uniqueId,
            int uniqueNumber,
            @NonNull String displayName,
            @NonNull UUID ownerId,
            @NonNull String ownerName) {
        Location defaultSpawn = Settings.getLevelDefaultSpawn().clone();
        defaultSpawn.setWorld(world);
        return new LevelSettings(
                new WorldSettings(
                        world, environment, defaultSpawn, Settings.getLevelDefaultDirection(), new ArrayList<>()),
                new GameSettings(
                        uniqueId, null, uniqueNumber, ownerId, ownerName, displayName, System.currentTimeMillis()));
    }

    public void updateParticleLocations() {
        this.getParticleController()
                .loadParticleLocations(this.getWorldSettings().getWaypoints());
    }
}
