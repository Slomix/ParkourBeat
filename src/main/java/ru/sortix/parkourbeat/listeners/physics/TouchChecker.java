package ru.sortix.parkourbeat.listeners.physics;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class TouchChecker {

    private static final double WHAT_IS_ACTUALLY_CLOSE = 1d;
    private final Set<Material> introverts;

    public List<BlockFace> getTouchingNeighbourIntroverts(Location pos, BoundingBox aabb) {
        List<BlockFace> neighbours = new ArrayList<>();
        Block block = pos.getBlock();
        for (BlockFace face : BlockFace.values()) {
            if (!face.isCartesian()) continue;
            Block neighbour = block.getRelative(face);

            // Check if this neighbor is in a set of introverts,
            // so We should detect and punish every touch!
            if (!introverts.contains(neighbour.getType())) continue;

            // Check the social distance between the player and the bouncy-goo
            // and see if the player can't (and isn't) touch(ing) this introvert :D
            if (cantTouchThis(aabb, BoundingBox.of(neighbour))) continue;

            // Player is actually distracting the other side of the block!
            // This is Einstein's relativity theory... go inverted when switching
            // the relativity point to its opposite.
            neighbours.add(face.getOppositeFace());
        }
        return neighbours;
    }

    private boolean cantTouchThis(BoundingBox aabb, BoundingBox anotherAaBb) {
        // Check if player can't touch this :(
        return !aabb.expand(WHAT_IS_ACTUALLY_CLOSE, WHAT_IS_ACTUALLY_CLOSE * 2, WHAT_IS_ACTUALLY_CLOSE).overlaps(anotherAaBb);
    }

}
