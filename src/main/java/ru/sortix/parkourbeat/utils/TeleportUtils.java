package ru.sortix.parkourbeat.utils;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.listeners.WorldsListener;

@UtilityClass
public class TeleportUtils {
    private final boolean ASYNC_TELEPORT_SUPPORTED;

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

    private boolean teleportSync(@NonNull Plugin plugin, @NonNull Player player, @NonNull Location location) {
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

    @NonNull public CompletableFuture<Boolean> teleportAsync(
            @NonNull Plugin plugin, @NonNull Player player, @NonNull Location location) {
        player.setFallDistance(0f);
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
                    .runTaskLater(plugin, () -> result.complete(teleportSync(plugin, player, location)), 1L);
        }
        return result;
    }

    @NonNull private String toString(@NonNull Location loc) {
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
