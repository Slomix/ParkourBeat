package ru.sortix.parkourbeat.world;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

@UtilityClass
public class LocationUtils {
    @SuppressWarnings("RedundantIfStatement")
    public boolean isValidSpawnPoint(@NonNull Location spawnLocation,
                                     @NonNull LevelSettings levelSettings
    ) {
        if (!levelSettings.getDirectionChecker()
            .isCorrectDirection(spawnLocation, levelSettings.getStartWaypointLoc())
        ) {
            return false;
        }

        if (BoundingBoxUtils.isBoundingBoxOverlapsWithAnyBlock(
            spawnLocation.getWorld(),
            BoundingBoxUtils.createBoundingBoxAtPos(0.6F, 1.8F, 0.6F, spawnLocation),
            true,
            true
        )) {
            return false;
        }

        return true;
    }
}
