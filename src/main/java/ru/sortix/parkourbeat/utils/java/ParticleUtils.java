package ru.sortix.parkourbeat.utils.java;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

@UtilityClass
public class ParticleUtils {
    private final boolean dustOptionsSupport =
            ClassUtils.isClassPresent("org.bukkit.Particle$DustOptions");

    public void displayRedstoneParticles(
            @NonNull Player player,
            @NonNull Color color,
            @NonNull Iterable<Location> locations,
            double maxDistanceSquared) {
        Location playerLoc = player.getLocation();

        if (color.getRed() == 0) color = color.setRed(1);

        double offsetX = (float) color.getRed() / 255.0;
        double offsetY = (float) color.getGreen() / 255.0;
        double offsetZ = (float) color.getBlue() / 255.0;

        if (dustOptionsSupport) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1f);
            for (Location location : locations) {
                if (location.distanceSquared(playerLoc) > maxDistanceSquared) continue;
                player.spawnParticle(
                        Particle.REDSTONE, location, 0, offsetX, offsetY, offsetZ, 1, dustOptions);
            }
        } else {
            for (Location location : locations) {
                if (location.distanceSquared(playerLoc) > maxDistanceSquared) continue;
                player.spawnParticle(Particle.REDSTONE, location, 0, offsetX, offsetY, offsetZ, 1);
            }
        }
    }
}
