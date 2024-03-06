package ru.sortix.parkourbeat.utils;

import java.util.logging.Logger;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.listeners.WorldsListener;

// TODO Temporary class. See: https://github.com/Slomix/ParkourBeat/issues/42
public class TeleportUtils {
    public static Logger logger;

    public static boolean teleport(@NonNull Player player, @NonNull Location location) {
        Location sourceLoc = player.getLocation();
        WorldsListener.CHUNKS_LOADED = 0;
        long startedAtMills = System.currentTimeMillis();
        boolean result = player.teleport(location);
        long durationMills = System.currentTimeMillis() - startedAtMills;
        if (WorldsListener.CHUNKS_LOADED > 0) {
            logger.warning(
                    "Телепортация игрока из "
                            + toString(sourceLoc)
                            + " в "
                            + toString(location)
                            + " заняла "
                            + durationMills
                            + " мс (загружено "
                            + WorldsListener.CHUNKS_LOADED
                            + " чанков)");
        }
        return result;
    }

    @NonNull private static String toString(@NonNull Location loc) {
        return "|"
                + (loc.getWorld() == null ? "null" : loc.getWorld().getName())
                + " "
                + loc.getBlockX()
                + " "
                + loc.getBlockY()
                + " "
                + loc.getBlockZ()
                + "|";
    }
}
