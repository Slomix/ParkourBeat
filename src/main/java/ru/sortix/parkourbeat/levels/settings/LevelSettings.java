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
    private final @NonNull WorldSettings worldSettings;
    private final @NonNull GameSettings gameSettings;
    private final @NonNull ParticleController particleController;
    private final @NonNull DirectionChecker directionChecker;
    private final @NonNull Location startWaypoint;
    private final @NonNull Location finishWaypoint;

    public LevelSettings(@NonNull ParkourBeat plugin,
                         @NonNull World world,
                         @NonNull WorldSettings worldSettings,
                         @NonNull GameSettings gameSettings
    ) {
        this.worldSettings = worldSettings;
        this.gameSettings = gameSettings;
        this.directionChecker = new DirectionChecker(worldSettings.getDirection());
        this.particleController =
            new ParticleController(plugin, world, this.directionChecker);
        this.startWaypoint = worldSettings.getStartWaypoint().toLocation(world);
        this.finishWaypoint = worldSettings.getFinishWaypoint().toLocation(world);
        // optional check is list sorted
        this.worldSettings.sortWaypoints(this.directionChecker);
    }

    @NonNull
    public static LevelSettings create(
        @NonNull ParkourBeat plugin,
        @NonNull World world,
        @NonNull World.Environment environment,
        @NonNull UUID uniqueId,
        int uniqueNumber,
        @NonNull String displayName,
        @NonNull UUID ownerId,
        @NonNull String ownerName
    ) {
        return new LevelSettings(
            plugin,
            world,
            Settings.getLevelDefaultSettings().setWorld(environment, world),
            new GameSettings(
                uniqueId,
                null,
                uniqueNumber,
                ownerId,
                ownerName,
                displayName,
                System.currentTimeMillis()
            )
        );
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
