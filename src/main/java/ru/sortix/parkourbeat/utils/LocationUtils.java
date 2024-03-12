package ru.sortix.parkourbeat.utils;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public class LocationUtils {

    public static boolean isValidSpawnPoint(@NonNull Location spawnLocation, @NonNull LevelSettings levelSettings) {
        DirectionChecker directionChecker = levelSettings.getDirectionChecker();
        Location startLocation = levelSettings.getWorldSettings().getStartBorderLoc();
        Block block = spawnLocation.getBlock();

        return !block.getRelative(BlockFace.DOWN).isPassable()
                && block.isPassable()
                && block.getRelative(BlockFace.UP).isPassable()
                && directionChecker.isCorrectDirection(spawnLocation, startLocation);
    }
}
