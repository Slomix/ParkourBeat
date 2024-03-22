package ru.sortix.parkourbeat.world;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

@AllArgsConstructor
@RequiredArgsConstructor
public class Cuboid {
    private @NonNull Vector min;
    private @NonNull Vector max;
    private @Nullable World world;

    @SuppressWarnings("RedundantIfStatement")
    public boolean isInside(@NonNull Location location) {
        if (this.world != null && location.getWorld() != null && this.world != location.getWorld()) return false;

        if (location.getX() < this.min.getX()) return false;
        if (location.getX() > this.max.getX()) return false;
        if (location.getZ() < this.min.getZ()) return false;
        if (location.getZ() > this.max.getZ()) return false;
        if (location.getY() < this.min.getY()) return false;
        if (location.getY() > this.max.getY()) return false;

        return true;
    }
}
