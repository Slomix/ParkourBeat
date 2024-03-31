package ru.sortix.parkourbeat.physics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.SpectateActivity;

import java.util.List;
import java.util.Set;

public class BouncePhysics {

    private final VelocityCalculator velocityCalculator;
    private final CollisionChecker collisionChecker;

    public BouncePhysics(ParkourBeat plugin, VelocityCalculator velocityCalculator, BoundingBoxRegistry bbRegistry) {
        ActivityManager activityManager = plugin.get(ActivityManager.class);
        this.velocityCalculator = velocityCalculator;
        this.collisionChecker = new CollisionChecker(Set.of(
            Material.SLIME_BLOCK,
            Material.LIGHT_BLUE_CONCRETE
        ), bbRegistry);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
            () -> {
                activityManager.getAllActivities().forEach(activity -> {
                    if (activity instanceof SpectateActivity) return;
                    Player player = activity.getPlayer();
                    doBounceLogic(player);
                });
            },
            1L,
            1L);
    }

    private void doBounceLogic(Player player) {
        Vector delta = velocityCalculator.getVelocity(player);
        if (delta == null) return;

        List<CollisionChecker.Collision> collisions = collisionChecker.getCollisions(player);
        if (collisions.isEmpty()) return;

        // TODO: Bouncing logic
    }

}
