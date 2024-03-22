package ru.sortix.parkourbeat.utils;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

@Deprecated // TODO Use PointXYZYawPitch
public class NonWorldLocation extends Location {
    public NonWorldLocation(double x, double y, double z, float yaw, float pitch) {
        super(null, x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.remove("world");
        return result;
    }

    @SuppressWarnings("unused")
    @NotNull public static NonWorldLocation deserialize(@NotNull Map<String, Object> args) {
        return new NonWorldLocation(
                NumberConversions.toDouble(args.get("x")),
                NumberConversions.toDouble(args.get("y")),
                NumberConversions.toDouble(args.get("z")),
                NumberConversions.toFloat(args.get("yaw")),
                NumberConversions.toFloat(args.get("pitch")));
    }
}
