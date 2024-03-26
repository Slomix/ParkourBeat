package ru.sortix.parkourbeat.levels;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Location;

@Getter
public class Waypoint {
    @Setter
    private Location location;
    private final Color color;
    @Setter
    private double height;

    public Waypoint(@NonNull Location location, double height, @NonNull Color color) {
        this.location = location;
        this.color = color;
        this.height = height;
    }
}
