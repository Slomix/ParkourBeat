package ru.sortix.parkourbeat.physics;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.physics.debug.BoundingBoxVisualizer;
import ru.sortix.parkourbeat.physics.debug.DebugSubjectKind;
import ru.sortix.parkourbeat.physics.debug.DebugViewerRegistry;
import ru.sortix.parkourbeat.physics.debug.VelocityVisualizer;

import java.util.UUID;

@Getter
public class CustomPhysicsManager implements PluginManager {

    private final BoundingBoxRegistry boundingBoxRegistry = new BoundingBoxRegistry();
    private final DebugViewerRegistry debugViewerRegistry = new DebugViewerRegistry();
    private final BoundingBoxVisualizer boundingBoxVisualizer;
    private final VelocityVisualizer velocityVisualizer;
    private final VelocityCalculator velocityCalculator;

    public CustomPhysicsManager(@NotNull ParkourBeat plugin) {
        boundingBoxVisualizer = new BoundingBoxVisualizer(plugin, debugViewerRegistry, boundingBoxRegistry);
        velocityVisualizer = new VelocityVisualizer(plugin, debugViewerRegistry);
        velocityCalculator = new VelocityCalculator(plugin, new BoundingBoxStretcher(boundingBoxRegistry), velocityVisualizer);
        new BouncePhysics(plugin, velocityCalculator, boundingBoxRegistry);
    }

    @Override
    public void disable() {
        debugViewerRegistry.purgeAll();
        boundingBoxRegistry.purgeAll();
        velocityCalculator.purgeAll();
        velocityVisualizer.purgeAll();
    }

    public void addPlayer(Player player, Level level) {
        if (level != null && !level.getLevelSettings().getGameSettings().isCustomPhysicsEnabled()) return;

        boundingBoxRegistry.update(player, player.getBoundingBox());
        boundingBoxVisualizer.toggleBoxRendering(player.getUniqueId(), DebugSubjectKind.PLAYER_BOX);
        velocityVisualizer.addPlayer(player);
        velocityCalculator.addPlayer(player);
    }

    public void purgePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boundingBoxRegistry.purge(player);
        if (boundingBoxVisualizer.isBoxRendered(uuid))
            boundingBoxVisualizer.toggleBoxRendering(uuid, DebugSubjectKind.PLAYER_BOX);
        velocityCalculator.purge(player);
        velocityVisualizer.purge(player);
    }

}
