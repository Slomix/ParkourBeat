package ru.sortix.parkourbeat.utils.java;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

@UtilityClass
public class ParticleUtils {
    private final boolean dustOptionsSupport = ClassUtils.isClassPresent("org.bukkit.Particle$DustOptions");

    public void displayRedstoneParticles(
        boolean legacyMode,
        @NonNull Player player,
        @NonNull Color color,
        float size,
        @NonNull Iterable<Location> locations,
        double maxDistanceSquared
    ) {
        Location playerLoc = player.getLocation();

        double offsetX = color.getRed() / 255.0;
        double offsetY = color.getGreen() / 255.0;
        double offsetZ = color.getBlue() / 255.0;

        if (dustOptionsSupport) {
            if (legacyMode) { // For clients 1.12.2 and older
                size = 1.0f; // Not actually a size, but colors contrast
                if (color.getRed() == 0) color = color.setRed(1);
            }
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, size);
            for (Location location : locations) {
                if (location.distanceSquared(playerLoc) > maxDistanceSquared) continue;
                player.spawnParticle(Particle.REDSTONE, location, 0, offsetX, offsetY, offsetZ, 1, dustOptions);
            }
        } else {
            for (Location location : locations) {
                if (location.distanceSquared(playerLoc) > maxDistanceSquared) continue;
                player.spawnParticle(Particle.REDSTONE, location, 0, offsetX, offsetY, offsetZ, 1);
            }
        }
    }
}
