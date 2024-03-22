package ru.sortix.parkourbeat.utils;

import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.world.PointXYZYawPitch;

@UtilityClass
public class ConfigUtils {
    @NonNull public Vector parsePointXYZ(@Nullable String input) {
        if (input == null) throw new IllegalArgumentException("Input not found");
        String[] args = input.split(" ");
        if (args.length != 3) throw new IllegalArgumentException("Wrong arguments amount");
        return new Vector(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
    }

    @NonNull public PointXYZYawPitch parsePointXYZYawPitch(@Nullable String input) {
        if (input == null) throw new IllegalArgumentException("Input not found");
        String[] args = input.split(" ");
        if (args.length == 3) {
            return new PointXYZYawPitch(
                    Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), 0, 0);
        } else if (args.length == 5) {
            return new PointXYZYawPitch(
                    Double.parseDouble(args[0]),
                    Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]),
                    Float.parseFloat(args[3]),
                    Float.parseFloat(args[4]));
        } else {
            throw new IllegalArgumentException("Wrong arguments amount");
        }
    }

    @NonNull public DirectionChecker.Direction parseDirection(
        @NonNull ConfigurationSection section, @NonNull String key) {
        try {
            return DirectionChecker.Direction.valueOf(section.getString(key));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid direction: " + key);
        }
    }
}
