package ru.sortix.parkourbeat.listeners.physics;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.ParkourBeat;

import java.util.List;
import java.util.Set;

// Adds a friction mechanic to ice walls
public class FrictionSimulatorListener implements Listener {

    private static final double WHAT_IS_ACTUALLY_ASKEW = Math.toRadians(45);
    private static final double INVERSE_WHAT_IS_ACTUALLY_ASKEW = Math.toRadians(180) - WHAT_IS_ACTUALLY_ASKEW;
    private static final TouchChecker checker = new TouchChecker(Set.of(
        Material.ICE,
        Material.BLUE_ICE,
        Material.FROSTED_ICE,
        Material.PACKED_ICE,
        Material.ORANGE_CONCRETE    // got a portal 2 reference?
    ));

    public FrictionSimulatorListener(@NotNull ParkourBeat plugin) {}

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().equals(event.getTo())) return;

        Player player = event.getPlayer();
        List<BlockFace> touching = checker.getTouchingNeighbourIntroverts(player.getLocation(), player.getBoundingBox());
        if (touching.isEmpty()) return;

        Vector velocity = event.getFrom().toVector().subtract(event.getTo().toVector());
        Vector normal = calculateNormal(touching);

        // A lot of cursed Maths related stuff.
        // WARNING: Dymeth is prohibited here due to
        // the high severity of mind-blowing!

        // You're pretty lucky that this is actually implemented by Bukkit!
        float playerWallSlope = normal.angle(velocity);
        if (playerWallSlope > WHAT_IS_ACTUALLY_ASKEW && playerWallSlope < INVERSE_WHAT_IS_ACTUALLY_ASKEW) return;

        // This formula is self-written from my imagination
        // at the 3 am (Europe/Moscow), so be aware of possible
        // ~~bugs~~ unintended features!
        Vector friction = velocity.subtract(normal.multiply(velocity.dot(normal) * 2));
        player.setVelocity(friction);
    }

    private Vector calculateNormal(List<BlockFace> touching) {
        Vector vec = touching.remove(0).getDirection();

        // Time for cursed text-based diagrams!
        // Cross-product of vectors gives the vector
        // that is perpendicular to both of them.
        //
        // -> x -> = ^
        // -> x ^ = |
        // \ x ^ = /

        // SELF-INVENTED AT 4.10 AM
        if (!touching.isEmpty()) {
            touching.forEach(face -> vec.add(face.getDirection()));
        }
        return vec.crossProduct(new Vector(0, 1, 0)).normalize();
    }

}