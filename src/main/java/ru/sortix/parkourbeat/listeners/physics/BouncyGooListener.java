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

// Makes slime blocks quite bouncy
public class BouncyGooListener implements Listener {

    private static final double FACTOR = 3d;
    private static final TouchChecker checker = new TouchChecker(Set.of(
        Material.SLIME_BLOCK,
        Material.LIGHT_BLUE_CONCRETE    // got a portal 2 reference? x2
    ));

    public BouncyGooListener(@NotNull ParkourBeat plugin) {}

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getBlock().equals(event.getFrom().getBlock())) return;
        Player player = event.getPlayer();
        List<BlockFace> slimes = checker.getTouchingNeighbourIntroverts(event.getTo(), player.getBoundingBox());
        if (slimes.isEmpty()) return;
        Vector normal = calculateBounceNormal(slimes);
        Vector velocity = event.getFrom().toVector().subtract(event.getTo().toVector());

        Vector bounceVelocity = normal.multiply(velocity.dot(normal) * 2).subtract(velocity);
        player.setVelocity(bounceVelocity.multiply(FACTOR));
    }

    private Vector calculateBounceNormal(List<BlockFace> slimes) {
        Vector velocity = new Vector();
        slimes.forEach(face -> velocity.add(face.getDirection()));
        return velocity.normalize();
    }

}
