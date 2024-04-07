package ru.sortix.parkourbeat.physics;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class BoundingBoxStretcher {

    private static final double EXPANSION_FACTOR = 2.5d;

    private final BoundingBoxRegistry registry;

    public void updateBoundingBox(Player player, Vector velocity) {
        BoundingBox box = player.getBoundingBox().clone().expand(0.01).expand(velocity, EXPANSION_FACTOR);
        registry.update(player, box);
    }
    
}
