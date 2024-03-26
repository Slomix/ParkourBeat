package ru.sortix.parkourbeat.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.Waypoint;

import javax.annotation.Nullable;
import java.util.Arrays;

@SuppressWarnings("unused")
@UtilityClass
public class ConfigUtils {
    @NonNull
    public String serializeVector(@NonNull Vector object) {
        return object.getX() + " " + object.getY() + " " + object.getZ();
    }

    @NonNull
    public Vector parseVector(@Nullable String input) {
        if (input == null) throw new IllegalArgumentException("Input not found");
        String[] args = input.split(" ");
        if (args.length != 3) throw new IllegalArgumentException("Wrong arguments amount");
        return new Vector(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
    }

    @NonNull
    public String serializeLocation(boolean serializeWorld, @NonNull Location object) {
        return (serializeWorld ? object.getWorld().getName() + " " : "")
            + object.getX() + " " + object.getY() + " " + object.getZ()
            + " " + object.getYaw() + " " + object.getPitch();
    }

    @NonNull
    public Location parseLocation(boolean parseWorld, @Nullable String input) {
        if (input == null) throw new IllegalArgumentException("Input not found");

        String[] args = input.split(" ");
        if (args.length < 1) throw new IllegalArgumentException("Wrong arguments amount");

        World world = null;
        if (parseWorld) {
            world = Bukkit.getWorld(args[0]);
            if (world == null) {
                throw new IllegalArgumentException("World of parsed location not loaded: " + args[0]);
            }
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (args.length == 3) {
            return new Location(world,
                Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]),
                0, 0
            );
        } else if (args.length == 5) {
            return new Location(world,
                Double.parseDouble(args[0]),
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2]),
                Float.parseFloat(args[3]),
                Float.parseFloat(args[4])
            );
        } else {
            throw new IllegalArgumentException("Wrong arguments amount");
        }
    }

    @NonNull
    public String serializeWaypoint(@NonNull Waypoint object) {
        Location loc = object.getLocation();
        return loc.getX() + " " + loc.getY() + " " + loc.getZ()
            + " " + object.getHeight()
            + " " + serializeBukkitHexColor(object.getColor());
    }

    @NonNull
    public Waypoint parseWaypoint(@NonNull String input) {
        String[] args = input.split(" ");
        if (args.length != 5) {
            throw new IllegalArgumentException("Wrong waypoint args amount");
        }
        return new Waypoint(new Location(null,
            Double.parseDouble(args[0]),
            Double.parseDouble(args[1]),
            Double.parseDouble(args[2])
        ),
            Double.parseDouble(args[3]),
            parseBukkitHexColor(args[4])
        );
    }

    @NonNull
    public String serializeBukkitHexColor(@NonNull Color object) {
        return "#" + String.format("%06X", object.asRGB());
    }

    @NonNull
    public Color parseBukkitHexColor(@NonNull String input) {
        if (input.length() != 7 || input.charAt(0) != '#') {
            throw new IllegalArgumentException("Wrong HEX-color format: " + input);
        }
        return Color.fromRGB(Integer.parseInt(input.substring(1), 16));
    }

    @NonNull
    public <E extends Enum<E>> String serializeEnum(@NonNull E object) {
        return object.name();
    }

    @NonNull
    public <E extends Enum<E>> E parseEnum(@NonNull Class<E> enumClass, @NonNull ConfigurationSection section, @NonNull String key) {
        String valueName = section.getString(key);
        if (valueName == null)
            throw new IllegalArgumentException("Value of enum " + enumClass.getName() + " not found");
        try {
            return Enum.valueOf(enumClass, valueName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Wrong " + enumClass.getName() + " name: " + valueName);
        }
    }
}
