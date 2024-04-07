package ru.sortix.parkourbeat.physics.debug;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.constant.DurationConstants;
import ru.sortix.parkourbeat.physics.BoundingBoxRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BoundingBoxVisualizer {

    private static final double PARTICLE_DISTANCE = 0.05f;

    private final Map<UUID, DebugSubjectKind> renderedBoxes = new HashMap<>();

    public BoundingBoxVisualizer(ParkourBeat plugin, DebugViewerRegistry debugViewerRegistry,
                                 BoundingBoxRegistry boundingBoxRegistry) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            plugin,
            () -> {
                if (debugViewerRegistry.shouldSkipDebug()) return;
                renderedBoxes.entrySet().removeIf((entry) -> {
                    UUID boxId = entry.getKey();
                    DebugSubjectKind kind = entry.getValue();

                    BoundingBox box = boundingBoxRegistry.getBoundingBox(boxId);
                    if (box == null) return true;
                    List<Vector> outline = getBoundingBoxOutline(box);
                    debugViewerRegistry.resolve().forEach(viewer -> render(viewer, outline, kind));
                    return false;
                });
            },
            DurationConstants.DEBUG_TASK_PERIOD,
            DurationConstants.DEBUG_TASK_PERIOD
        );
    }

    private void render(Player viewer, List<Vector> outline, DebugSubjectKind kind) {
        outline.forEach(point -> DebugParticleRenderer.showParticle(viewer, point, kind));
    }

    public boolean isBoxRendered(UUID boxId) {
        return renderedBoxes.containsKey(boxId);
    }

    public void toggleBoxRendering(UUID boxId, DebugSubjectKind kind) {
        if (renderedBoxes.remove(boxId) == null) renderedBoxes.put(boxId, kind);
    }

    public List<Vector> getBoundingBoxOutline(BoundingBox box) {
        List<Vector> outline = new ArrayList<>();
        for (double x = box.getMinX(); x <= box.getMaxX() + PARTICLE_DISTANCE; x = Math.round((x + PARTICLE_DISTANCE) * 1e4) / 1e4) {
            for (double y = box.getMinY(); y <= box.getMaxY() + PARTICLE_DISTANCE; y = Math.round((y + PARTICLE_DISTANCE) * 1e4) / 1e4) {
                for (double z = box.getMinZ(); z <= box.getMaxZ() + PARTICLE_DISTANCE; z = Math.round((z + PARTICLE_DISTANCE) * 1e4) / 1e4) {
                    int components = 0;
                    if (x == box.getMinX() || x >= box.getMaxX()) components++;
                    if (y == box.getMinY() || y >= box.getMaxY()) components++;
                    if (z == box.getMinZ() || z >= box.getMaxZ()) components++;
                    if (components >= 2) {
                        outline.add(new Vector(x, y, z));
                    }
                }
            }
        }
        return outline;
    }

}
