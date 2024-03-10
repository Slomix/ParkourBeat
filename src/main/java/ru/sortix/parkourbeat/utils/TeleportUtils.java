package ru.sortix.parkourbeat.utils;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.listeners.WorldsListener;

public class TeleportUtils {
    public static final boolean ASYNC_TELEPORT_SUPPORTED;

    static {
        boolean asyncTeleportSupported;
        try {
            Entity.class.getDeclaredMethod("teleportAsync", Location.class);
            asyncTeleportSupported = true;
        } catch (NoSuchMethodException e) {
            asyncTeleportSupported = false;
        }
        ASYNC_TELEPORT_SUPPORTED = asyncTeleportSupported;
    }

    public static Plugin plugin;

    private static boolean teleportSync(@NonNull Player player, @NonNull Location location) {
        Location sourceLoc = player.getLocation();
        WorldsListener.CHUNKS_LOADED = 0;
        long startedAtMills = System.currentTimeMillis();
        boolean success = player.teleport(location);
        long durationMills = System.currentTimeMillis() - startedAtMills;
        if (WorldsListener.CHUNKS_LOADED > 0 && ASYNC_TELEPORT_SUPPORTED) {
            plugin.getLogger()
                    .log(
                            Level.WARNING,
                            "Обнаружена телепортация игрока " + player.getName()
                                    + " из " + toString(sourceLoc)
                                    + " в " + toString(location)
                                    + " в основном потоке. Телепорт занял " + durationMills + " мс"
                                    + " (загружено " + WorldsListener.CHUNKS_LOADED + " чанков)",
                            new RuntimeException("Стек вызовов:"));
        }
        if (!success) {
            player.sendMessage("Телепортация отменена");
        }
        return success;
    }

    @NonNull public static CompletableFuture<Boolean> teleportAsync(@NonNull Player player, @NonNull Location location) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        if (ASYNC_TELEPORT_SUPPORTED) {
            player.teleportAsync(location).thenAccept(success -> {
                if (!success) {
                    player.sendMessage("Телепортация отменена");
                }
                result.complete(success);
            });
        } else {
            plugin.getServer()
                    .getScheduler()
                    .runTaskLater(plugin, () -> result.complete(teleportSync(player, location)), 1L);
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
