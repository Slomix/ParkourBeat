package parkourbeat.location;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import parkourbeat.ParkourBeat;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Position implements ConfigurationSerializable {

    private double x, y, z;
    private float yaw, pitch;

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Position(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public Location getInWorld(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("yaw", yaw);
        map.put("pitch", pitch);
        return map;
    }

    public static Position deserialize(Map<String, Object> map) {
        return new Position((double) map.get("x"), (double) map.get("y"), (double) map.get("z"), (float) map.get("yaw"), (float) map.get("pitch"));
    }
}
