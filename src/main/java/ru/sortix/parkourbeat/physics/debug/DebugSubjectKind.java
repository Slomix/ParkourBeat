package ru.sortix.parkourbeat.physics.debug;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Particle;

@Getter
@RequiredArgsConstructor
public enum DebugSubjectKind {

    PLAYER_BOX(Particle.REDSTONE, new Particle.DustOptions(Color.AQUA, .5f)),
    BOUNCE_BOX(Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, .5f)),
    FRICTION_BOX(Particle.REDSTONE, new Particle.DustOptions(Color.ORANGE, .5f)),
    MOVEMENT_VECTOR(Particle.REDSTONE, new Particle.DustOptions(Color.RED, .5f)),
    VELOCITY_VECTOR(Particle.REDSTONE, new Particle.DustOptions(Color.BLUE, .5f)),
    ;

    private final Particle particle;
    private final Object options;

    public boolean isVector() {
        return switch (this) {
            case MOVEMENT_VECTOR:
            case VELOCITY_VECTOR:
                yield true;
            default:
                yield false;
        };
    }

}
