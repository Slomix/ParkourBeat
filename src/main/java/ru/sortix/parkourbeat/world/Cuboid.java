package ru.sortix.parkourbeat.world;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

@AllArgsConstructor
@RequiredArgsConstructor
public class Cuboid {
    private @NonNull Vector min;
    private @NonNull Vector max;
    private @Nullable World world;

    @SuppressWarnings("RedundantIfStatement")
    public boolean isInside(@NonNull Location loc) {
        if (this.world != null && loc.getWorld() != null && this.world != loc.getWorld()) return false;

        if (loc.getX() < this.min.getX() || loc.getX() > this.max.getX()) return false;
        if (loc.getZ() < this.min.getZ() || loc.getZ() > this.max.getZ()) return false;
        if (loc.getY() < this.min.getY() || loc.getY() > this.max.getY()) return false;

        return true;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean isInside(double x, double y, double z) {

        if (x < this.min.getX() || x > this.max.getX()) return false;
        if (z < this.min.getZ() || z > this.max.getZ()) return false;
        if (y < this.min.getY() || y > this.max.getY()) return false;

        return true;
    }
}
