package parkourbeat.location;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Region implements ConfigurationSerializable {
    private final int minX, minY, minZ, maxX, maxY, maxZ;
    private final int sizeX, sizeY, sizeZ;

    public Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.sizeX = maxX - minX;
        this.sizeY = maxY - minY;
        this.sizeZ = maxZ - minZ;
    }

    public Position getCenter() {
        return new Position((minX + maxX) / 2.0, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0);
    }

    public boolean isOutside(double x, double y, double z) {
        return x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ;
    }

    public boolean isInside(double x, double y, double z) {
        return !isOutside(x, y, z);
    }

    public boolean isOutside(Location location) {
        return isOutside(location.getX(), location.getY(), location.getZ());
    }

    public boolean isInside(Location location) {
        return !isOutside(location);
    }

    protected boolean isNotOverlap(Region zone) {
        return zone.maxX < minX || zone.minX > maxX || zone.maxY < minY || zone.minY > maxY || zone.maxZ < minZ || zone.minZ > maxZ;
    }

    protected boolean isOverlap(Region zone) {
        return !isNotOverlap(zone);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("minX", minX);
        map.put("minY", minY);
        map.put("minZ", minZ);
        map.put("maxX", maxX);
        map.put("maxY", maxY);
        map.put("maxZ", maxZ);
        return map;
    }

    public static Region deserialize(Map<String, Object> map) {
        return new Region((int) map.get("minX"), (int) map.get("minY"), (int) map.get("minZ"), (int) map.get("maxX"), (int) map.get("maxY"), (int) map.get("maxZ"));
    }
}
