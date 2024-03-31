package ru.sortix.parkourbeat.physics;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class CollisionChecker {

    private final Set<Material> materials;
    private final BoundingBoxRegistry boundingBoxRegistry;

    public List<Collision> getCollisions(Player player) {
        List<Collision> collisions = getCollisions0(player, player.getLocation());
        collisions.addAll(getCollisions0(player, player.getEyeLocation()));
        return collisions;
    }

    private List<Collision> getCollisions0(Player player, Location pos) {
        UUID uuid = player.getUniqueId();
        BoundingBox box = boundingBoxRegistry.getBoundingBox(uuid);

        List<Collision> collisions = new ArrayList<>();
        Block block = pos.getBlock();
        for (BlockFace face : BlockFace.values()) {
            if (!face.isCartesian()) continue;
            Block rel = block.getRelative(face);
            if (!materials.contains(rel.getType())) continue;

            BoundingBox blockBox = BoundingBox.of(rel);
            if (!blockBox.overlaps(box)) continue;

            BoundingBox intersection = blockBox.intersection(box);
            collisions.add(new Collision(face.getOppositeFace(), intersection));
        }

        return collisions;
    }

    public record Collision(BlockFace face, BoundingBox intersection) {}

}
