package ru.sortix.parkourbeat.location;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

@Getter
public class Waypoint implements ConfigurationSerializable {

    @Setter private Location location;
    private final Color color;
    @Setter private double height;

    public Waypoint(Location location, Color color, double height) {
        this.location = location;
        this.color = color;
        this.height = height;
    }

    @SuppressWarnings("unused")
    public static Waypoint deserialize(Map<String, Object> map) {
        Location location = (Location) map.get("location");
        Color color = (Color) map.get("color");
        double height = (double) map.get("height");
        return new Waypoint(location, color, height);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("location", location);
        map.put("color", color);
        map.put("height", height);
        return map;
    }
}
