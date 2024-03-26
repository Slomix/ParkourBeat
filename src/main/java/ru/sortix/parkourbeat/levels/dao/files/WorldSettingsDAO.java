package ru.sortix.parkourbeat.levels.dao.files;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.levels.Waypoint;
import ru.sortix.parkourbeat.utils.ConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldSettingsDAO {
    public void write(@NonNull WorldSettings worldSettings, @NonNull ConfigurationSection section) {

        section.set("environment", ConfigUtils.serializeEnum(worldSettings.getEnvironment()));

        section.set("direction", ConfigUtils.serializeEnum(worldSettings.getDirection()));

        section.set("spawn", ConfigUtils.serializeLocation(false, worldSettings.getSpawn()));

        section.set(
            "waypoints",
            worldSettings.getWaypoints().stream()
                .map(ConfigUtils::serializeWaypoint)
                .collect(Collectors.toList()));
    }

    @NonNull
    public WorldSettings read(@NonNull ConfigurationSection section) {

        World.Environment environment
            = ConfigUtils.parseEnum(World.Environment.class, section, "environment");

        DirectionChecker.Direction direction
            = ConfigUtils.parseEnum(DirectionChecker.Direction.class, section, "direction");

        Location spawn = ConfigUtils.parseLocation(false, section.getString("spawn"));

        List<Waypoint> waypoints = new ArrayList<>();
        for (String waypoint : section.getStringList("waypoints")) {
            waypoints.add(ConfigUtils.parseWaypoints(waypoint));
        }

        return new WorldSettings(environment, direction, spawn, waypoints);
    }
}
