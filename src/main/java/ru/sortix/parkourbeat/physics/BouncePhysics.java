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

    private static final double BOUNCINESS = 2f;

    private final VelocityCalculator velocityCalculator;
    private final CollisionChecker collisionChecker;
    private final ParkourBeat plugin;

    public BouncePhysics(ParkourBeat plugin, VelocityCalculator velocityCalculator, BoundingBoxRegistry bbRegistry) {
        this.plugin = plugin;
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

        Vector wallVolumetricNormal = mergeCollisionVelocities(collisions);
        Vector bounce = delta.clone().add(wallVolumetricNormal)
            .subtract(wallVolumetricNormal.multiply(delta.dot(wallVolumetricNormal) * 2)).multiply(BOUNCINESS);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
            () -> player.setVelocity(bounce), 1L);
    }

    private Vector mergeCollisionVelocities(List<CollisionChecker.Collision> collisions) {
        Vector velocity = new Vector();
        double volume = 0;
        for (CollisionChecker.Collision collision : collisions) {
            volume += collision.intersection().getVolume();
            velocity.add(collision.face().getDirection());
        }

        if (velocity.lengthSquared() == 0) return velocity;
        return velocity.normalize().multiply(BOUNCINESS * volume);
    }

}
