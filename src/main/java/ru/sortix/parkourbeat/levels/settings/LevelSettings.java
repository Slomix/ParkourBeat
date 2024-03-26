package ru.sortix.parkourbeat.levels.settings;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.ParticleController;

import java.util.UUID;

@Getter
public class LevelSettings {
    private final WorldSettings worldSettings;
    private final GameSettings gameSettings;
    private final ParticleController particleController;
    private final DirectionChecker directionChecker;
    private final Location startWaypoint;
    private final Location finishWaypoint;
    public LevelSettings(@NonNull World world, @NonNull WorldSettings worldSettings, @NonNull GameSettings gameSettings) {
        this.worldSettings = worldSettings;
        this.gameSettings = gameSettings;
        this.directionChecker = new DirectionChecker(worldSettings.getDirection());
        this.particleController =
            new ParticleController(ParkourBeat.getPlugin(), world, this.directionChecker);
        this.startWaypoint = worldSettings.getStartWaypoint().toLocation(world);
        this.finishWaypoint = worldSettings.getFinishWaypoint().toLocation(world);
        // optional check is list sorted
        this.worldSettings.sortWaypoints(this.directionChecker);
    }

    @NonNull
    public static LevelSettings create(
        @NonNull World world,
        @NonNull World.Environment environment,
        @NonNull UUID uniqueId,
        int uniqueNumber,
        @NonNull String displayName,
        @NonNull UUID ownerId,
        @NonNull String ownerName
    ) {
        return new LevelSettings(
            world,
            Settings.getLevelDefaultSettings().setWorld(environment, world),
            new GameSettings(
                uniqueId, null, uniqueNumber, ownerId, ownerName, displayName, System.currentTimeMillis()));
    }

    public void updateParticleLocations() {
        this.getParticleController()
            .loadParticleLocations(this.getWorldSettings().getWaypoints());
    }

    @NonNull
    public Location getStartWaypointLoc() {
        return this.startWaypoint;
    }

    @NonNull
    public Location getFinishWaypointLoc() {
        return this.finishWaypoint;
    }
}
