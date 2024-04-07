package ru.sortix.parkourbeat.physics.debug;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DebugViewerRegistry {

    private final Set<UUID> debuggers = new HashSet<>();

    public boolean toggleDebug(Player player) {
        UUID uuid = player.getUniqueId();
        if (!debuggers.remove(uuid)) {
            debuggers.add(uuid);
            return true;
        }
        return false;
    }

    public boolean shouldSkipDebug() {
        return debuggers.isEmpty();
    }

    public Set<Player> resolve() {
        Set<Player> resolved = new HashSet<>();
        debuggers.removeIf(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return true;
            resolved.add(player);
            return false;
        });
        return resolved;
    }

    public void purgeAll() {
        debuggers.clear();
    }

}
