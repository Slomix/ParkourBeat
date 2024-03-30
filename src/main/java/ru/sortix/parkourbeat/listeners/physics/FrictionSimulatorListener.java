package ru.sortix.parkourbeat.listeners.physics;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.ParkourBeat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Adds a friction mechanic to ice walls
public class FrictionSimulatorListener implements Listener {

    private static final double WHAT_IS_ACTUALLY_ASKEW = Math.toRadians(45);
    private static final double INVERSE_WHAT_IS_ACTUALLY_ASKEW = Math.toRadians(180) - WHAT_IS_ACTUALLY_ASKEW;
    private static final TouchChecker checker = new TouchChecker(Set.of(
        Material.ICE,
        Material.BLUE_ICE,
        Material.FROSTED_ICE,
        Material.PACKED_ICE,
        Material.ORANGE_CONCRETE
    ));

    private final Set<UUID> gliding = new HashSet<>();
    private final CustomPhysicsChecker physicsChecker;

    public FrictionSimulatorListener(@NotNull ParkourBeat plugin) {
        this.physicsChecker = new CustomPhysicsChecker(plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;

        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();
        List<BlockFace> touching = checker.getTouchingEntireBody(player);
        if (touching.isEmpty()) {
            if (gliding.contains(uuid)) {
                player.setGravity(true);
                gliding.remove(uuid);
            }
            return;
        }

        // Only run if custom physics is enabled
        if (!physicsChecker.getCustomPhysicsRule(player)) return;

        Vector velocity = event.getFrom().toVector().subtract(event.getTo().toVector());
        Vector normal = calculateNormal(touching);

        // You're pretty lucky that this is actually implemented by Bukkit!
        // But just in case... arc cosine of dot / (magnitude product)
        float playerWallSlope = normal.angle(velocity);
        if (playerWallSlope > WHAT_IS_ACTUALLY_ASKEW && playerWallSlope < INVERSE_WHAT_IS_ACTUALLY_ASKEW) return;

        // Be aware of ~~bugs~~ unintended features as it is SELF-INVENTED!
        Vector friction = velocity.clone().subtract(normal.multiply(velocity.dot(normal) * 2));
        if (friction.equals(velocity)) {
            // Nothing changed, We do not want to interrupt player's movement
            return;
        }
        gliding.add(player.getUniqueId());
        player.setGravity(false);
        player.setVelocity(friction);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        event.getPlayer().setGravity(true);
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
        //
        // Basically in some cases that provides a decent rotation
        // without the use of matrices and quaternions.

        if (!touching.isEmpty()) {
            touching.forEach(face -> vec.add(face.getDirection()));
        }
        vec.crossProduct(new Vector(0, 1, 0));
        if (vec.equals(new Vector())) return vec.zero();
        return vec.normalize();
    }

}
