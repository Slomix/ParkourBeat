package ru.sortix.parkourbeat.world;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BoundingBoxUtils {
    @NonNull
    public BoundingBox createBoundingBoxAtPos(@NonNull Entity entity, @NonNull Location at) {
        BoundingBox bb = entity.getBoundingBox();
        return createBoundingBoxAtPos(bb.getWidthX(), bb.getHeight(), bb.getWidthZ(), at);
    }

    @NonNull
    public BoundingBox createBoundingBoxAtPos(double widthX, double height, double widthZ, @NonNull Location at) {
        double x = at.getX();
        double y = at.getY();
        double z = at.getZ();
        double halfWidthX = widthX / 2.0F;
        double halfWidthZ = widthZ / 2.0F;
        return new BoundingBox(
            x - halfWidthX, y, z - halfWidthZ,
            x + halfWidthX, y + height, z + halfWidthZ
        );
    }

    public boolean isBoundingBoxOverlapsWithAnyBlock(@NonNull World world,
                                                     @NonNull BoundingBox box,
                                                     boolean ignoreNotOverlaps,
                                                     boolean ignorePassable
    ) {
        for (Block block : getBlocksInBoundingBox(world, box, ignoreNotOverlaps)) {
            if (ignorePassable && block.isPassable()) {
                continue;
            }
            return true;
        }
        return false;
    }

    @NonNull
    public List<Block> getBlocksInBoundingBox(@NonNull World world, @NonNull BoundingBox box, boolean ignoreNotOverlaps) {
        int minX = NumberConversions.floor(box.getMinX());
        int maxX = NumberConversions.floor(box.getMaxX());
        int minY = NumberConversions.floor(box.getMinY());
        int maxY = NumberConversions.floor(box.getMaxY());
        int minZ = NumberConversions.floor(box.getMinZ());
        int maxZ = NumberConversions.floor(box.getMaxZ());

        minY = Math.max(world.getMinHeight(), minY);
        maxY = Math.min(world.getMaxHeight(), maxY);

        List<Block> result = new ArrayList<>();
        Block block;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    block = world.getBlockAt(x, y, z);
                    if (ignoreNotOverlaps && !block.getBoundingBox().overlaps(box)) continue;
                    result.add(block);
                }
            }
        }
        return result;
    }
}
