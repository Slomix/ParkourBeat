package ru.sortix.parkourbeat.utils;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

@Deprecated // TODO Use Vector
public class NonWorldAndYawPitchLocation extends Location {
    public NonWorldAndYawPitchLocation(double x, double y, double z) {
        super(null, x, y, z);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.remove("world");
        return result;
    }

    @SuppressWarnings("unused")
    @NotNull public static NonWorldAndYawPitchLocation deserialize(@NotNull Map<String, Object> args) {
        return new NonWorldAndYawPitchLocation(
                NumberConversions.toDouble(args.get("x")),
                NumberConversions.toDouble(args.get("y")),
                NumberConversions.toDouble(args.get("z")));
    }
}
