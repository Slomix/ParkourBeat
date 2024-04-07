package ru.sortix.parkourbeat.physics;

import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoundingBoxRegistry {

    private final Map<UUID, BoundingBox> boundingBoxMap = new ConcurrentHashMap<>();

    public void update(Player player, BoundingBox box) {
        boundingBoxMap.put(player.getUniqueId(), box);
    }

    public void purge(Player player) {
        boundingBoxMap.remove(player.getUniqueId());
    }

    public BoundingBox getBoundingBox(UUID uuid) {
        return boundingBoxMap.get(uuid);
    }

    public void purgeAll() {
        this.boundingBoxMap.clear();
    }

}
