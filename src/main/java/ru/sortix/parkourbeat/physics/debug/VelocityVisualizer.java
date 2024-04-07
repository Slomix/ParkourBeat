package ru.sortix.parkourbeat.physics.debug;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.constant.DurationConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VelocityVisualizer {

    private static final double STEP = 0.02d;

    private final Map<UUID, OriginVec> movements = new HashMap<>();
    private final Set<UUID> players = new HashSet<>();

    public VelocityVisualizer(ParkourBeat plugin, DebugViewerRegistry debugViewerRegistry) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
            () -> {
                if (debugViewerRegistry.shouldSkipDebug()) return;
                players.removeIf(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) return true;

                    OriginVec deltaMovement = movements.remove(uuid);
                    Vector velocity = player.getVelocity();

                    Location origin = player.getLocation().add(0, 1, 0);
                    Set<Player> resolvedViewers = debugViewerRegistry.resolve();
                    if (deltaMovement != null)
                        renderVector(deltaMovement.vec, DebugSubjectKind.MOVEMENT_VECTOR,
                            deltaMovement.origin.toLocation(player.getWorld()), resolvedViewers);
                    renderVector(velocity, DebugSubjectKind.VELOCITY_VECTOR, origin, resolvedViewers);
                    return false;
                });
            },
            DurationConstants.DEBUG_TASK_PERIOD,
            DurationConstants.DEBUG_TASK_PERIOD);
    }

    private void renderVector(Vector vector, DebugSubjectKind kind, Location origin, Set<Player> resolvedViewers) {
        if (!kind.isVector()) return;
        List<Vector> points = getVecPoints(vector, origin);
        resolvedViewers.forEach(viewer -> {
            points.forEach(point -> {
                DebugParticleRenderer.showParticle(viewer, point, kind);
            });
        });
    }

    public void purge(Player player) {
        UUID uuid = player.getUniqueId();
        movements.remove(uuid);
        players.remove(uuid);
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    public void purgeAll() {
        movements.clear();
    }

    private List<Vector> getVecPoints(Vector vector, Location origin) {
        List<Vector> points = new ArrayList<>();

        double length = vector.length();
        Vector step = vector.clone().multiply(1/length).multiply(STEP);
        Vector pos = new Vector();
        int maxSteps = (int) Math.ceil(length / STEP);

        for (int i = 0; i < maxSteps; i++) {
            points.add(origin.toVector().add(pos));
            pos.add(step);
        }

        return points;
    }

    public void update(Player player, OriginVec vec) {
        movements.put(player.getUniqueId(), vec);
    }

    public record OriginVec(Vector origin, Vector vec) {}

}
