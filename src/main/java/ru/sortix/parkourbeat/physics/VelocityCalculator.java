package ru.sortix.parkourbeat.physics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.physics.debug.VelocityVisualizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VelocityCalculator {

    private final Map<UUID, Vector> positions = new HashMap<>();
    private final Map<UUID, Vector> velocities = new HashMap<>();
    private final Set<UUID> players = new HashSet<>();

    public VelocityCalculator(ParkourBeat plugin, BoundingBoxStretcher boundingBoxStretcher,
                              VelocityVisualizer velocityVisualizer) {
        // TODO: handle player quitting
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
            () -> players.removeIf(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) return true;

                Vector pos = player.getLocation().toVector();
                Vector prev = positions.getOrDefault(uuid, pos);
                positions.put(uuid, pos);

                Vector velocity = pos.clone().subtract(prev);
                velocityVisualizer.update(player, new VelocityVisualizer.OriginVec(prev.clone(), velocity.clone()));
                velocities.put(uuid, velocity);
                boundingBoxStretcher.updateBoundingBox(player, velocity);
                return false;
            }),
            1L,
            1L);
    }

    public void addPlayer(Player player) {
        this.players.add(player.getUniqueId());
    }

    public Vector getVelocity(Player player) {
        return velocities.get(player.getUniqueId());
    }

    public void purgeAll() {
        this.positions.clear();
        this.velocities.clear();
        this.players.clear();
    }

    public void purge(Player player) {
        UUID uuid = player.getUniqueId();
        this.velocities.remove(uuid);
        this.positions.remove(uuid);
        this.players.remove(uuid);
    }

}
