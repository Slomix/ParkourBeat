package ru.sortix.parkourbeat.world;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class PointXYZYawPitch extends Vector {
    protected float yaw;
    protected float pitch;

    public PointXYZYawPitch(double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @NonNull public Location toLocation(@NonNull World world) {
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }
}
