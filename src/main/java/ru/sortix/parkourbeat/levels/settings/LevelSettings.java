package ru.sortix.parkourbeat.levels.settings;

import java.util.ArrayList;
import java.util.Comparator;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.location.Waypoint;

@Getter
public class LevelSettings {

    private final WorldSettings worldSettings;
    private final GameSettings gameSettings;
    private final ParticleController particleController;
    private DirectionChecker directionChecker;

    public LevelSettings(WorldSettings worldSettings, GameSettings gameSettings) {
        this.worldSettings = worldSettings;
        this.gameSettings = gameSettings;
        this.directionChecker = new DirectionChecker(worldSettings.getDirection());
        this.particleController =
                new ParticleController(ParkourBeat.getPlugin(), this.directionChecker);

        // optional check is list sorted
        Comparator<Waypoint> comparator =
                Comparator.comparingDouble(
                        waypoint -> directionChecker.getCoordinate(waypoint.getLocation()));
        if (directionChecker.getDirection() == DirectionChecker.Direction.NEGATIVE_X
                || directionChecker.getDirection() == DirectionChecker.Direction.NEGATIVE_Z)
            comparator = comparator.reversed();
        worldSettings.getWaypoints().sort(comparator);
    }

    public static LevelSettings create(World world, String owner) {
        Location defaultSpawn = Settings.getDefaultWorldSpawn().clone();
        defaultSpawn.setWorld(world);
        return new LevelSettings(
                new WorldSettings(world, defaultSpawn, null, null, new ArrayList<>()),
                new GameSettings(null, null, owner));
    }

    public void updateDirectionChecker() {
        directionChecker = new DirectionChecker(worldSettings.getDirection());
        particleController.setDirectionChecker(directionChecker);
    }
}
