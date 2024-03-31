package ru.sortix.parkourbeat.physics.debug;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DebugParticleRenderer {

    private static final int PARTICLE_COUNT = 4;

    private static void showParticle(Player viewer, Vector pos, DebugSubjectKind kind, Object options) {
        viewer.spawnParticle(kind.getParticle(), pos.getX(), pos.getY(), pos.getZ(), PARTICLE_COUNT, options);
    }

    public static void showParticle(Player viewer, Vector pos, DebugSubjectKind kind) {
        showParticle(viewer, pos, kind, kind.getOptions());
    }

}
