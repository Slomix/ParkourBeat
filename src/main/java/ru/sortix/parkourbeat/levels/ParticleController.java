package ru.sortix.parkourbeat.levels;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.item.editor.type.EditTrackPointsItem;
import ru.sortix.parkourbeat.utils.java.ParticleUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class ParticleController {
    private static final double SEGMENT_LENGTH = 0.25;
    private static final double MAX_PARTICLES_VIEW_DISTANCE_SQUARED = Math.pow(10, 2);

    private final @NonNull ParkourBeat plugin;
    private final @NonNull ConcurrentLinkedQueue<Location> particleLocations = new ConcurrentLinkedQueue<>();
    private final @NonNull Map<Double, Color> colorsChangeLocations = new LinkedHashMap<>();
    private final @NonNull Set<Player> particleViewers = ConcurrentHashMap.newKeySet();
    @Getter
    private final @NonNull World world;
    private final @NonNull DirectionChecker directionChecker;
    private boolean isLoaded = false;

    public ParticleController(
        @NonNull ParkourBeat plugin,
        @NonNull World world,
        @NonNull DirectionChecker directionChecker
    ) {
        this.plugin = plugin;
        this.world = world;
        this.directionChecker = directionChecker;
    }

    @NonNull
    public static List<Location> createCurvedPath(@NonNull Location start, Location end, double height) {
        List<Location> path = new ArrayList<>();

        Vector startVector = start.toVector();
        Vector endVector = end.toVector();

        double length = startVector.distance(endVector); // Длина отрезка
        int segments = calculateSegments(length, height);

        // Определение точек управления для кубической интерполяции
        Vector control1 = startVector.clone().midpoint(endVector).add(new Vector(0, height, 0));
        Vector control2 = endVector.clone().midpoint(startVector).add(new Vector(0, height, 0));

        for (int t = 0; t <= segments; t++) {
            double ratio = t / (double) segments;

            Vector interpolated = cubicBezierInterpolation(startVector, control1, control2, endVector, ratio);

            Location location =
                new Location(start.getWorld(), interpolated.getX(), interpolated.getY(), interpolated.getZ());
            path.add(location);
        }

        return path;
    }

    private static int calculateSegments(double length, double height) {
        // Рассчитываем количество сегментов на основе длины и высоты дуги
        double totalLength = Math.sqrt(length * length + height * height);
        int segments = (int) Math.ceil(totalLength / SEGMENT_LENGTH);
        // Гарантируем, что хотя бы один сегмент
        segments = Math.max(segments, 1);

        return segments;
    }

    @NonNull
    private static Vector cubicBezierInterpolation(@NonNull Vector p0,
                                                   @NonNull Vector p1,
                                                   @NonNull Vector p2,
                                                   @NonNull Vector p3,
                                                   double t
    ) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        Vector p = p0.clone().multiply(uuu);
        p.add(p1.clone().multiply(3 * uu * t));
        p.add(p2.clone().multiply(3 * u * tt));
        p.add(p3.clone().multiply(ttt));

        return p;
    }

    public void loadParticleLocations(@NonNull List<Waypoint> waypoints) {
        this.stopSpawnParticles();

        if (this.isLoaded) {
            this.isLoaded = false;
            this.particleLocations.clear();
            this.colorsChangeLocations.clear();
        }

        Color previousColor = null;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint currentPoint = waypoints.get(i);
            Waypoint nextPoint = waypoints.get(i + 1);
            if (!currentPoint.getColor().equals(previousColor)) {
                this.colorsChangeLocations.put(
                    this.directionChecker.getCoordinate(currentPoint.getLocation()), currentPoint.getColor());
                previousColor = currentPoint.getColor();
            }

            double height = currentPoint.getHeight();
            if (height == 0) {
                List<Location> straightPath = createStraightPath(currentPoint.getLocation(), nextPoint.getLocation());
                this.particleLocations.addAll(straightPath);
            } else {
                List<Location> curvedPath =
                    createCurvedPath(currentPoint.getLocation(), nextPoint.getLocation(), height);
                this.particleLocations.addAll(curvedPath);
            }
        }
        this.isLoaded = true;
        this.plugin.get(LevelsManager.class).addParticleController(this);
    }

    private static long LAST_STACK_PRINTED_AT = 0;

    public void tickParticles() {
        if (!this.isLoaded) {
            this.plugin.getLogger().severe(
                "Unable to tick particles in world " + this.world.getName() + ": "
                    + "Controller not loaded");
            return;
        }

        for (Player player : this.particleViewers) {
            try {
                this.displayPlayerParticles(player);
            } catch (Exception e) {
                if (System.currentTimeMillis() - LAST_STACK_PRINTED_AT > 5_000) {
                    LAST_STACK_PRINTED_AT = System.currentTimeMillis();
                    this.plugin.getLogger().log(Level.SEVERE,
                        "Unable to tick particles in world " + this.world.getName()
                            + " of player " + player.getName(), e);
                } else {
                    this.plugin.getLogger().log(Level.SEVERE,
                        "Unable to tick particles in world " + this.world.getName()
                            + " of player " + player.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private void displayPlayerParticles(@NonNull Player player) {
        if (!player.isOnline()) {
            throw new IllegalStateException("Player is not online!");
        }
        if (player.getWorld() != this.world) {
            throw new IllegalStateException("Wrong player world: " + player.getWorld().getName());
        }

        Color color = getCurrentColor(player.getLocation());

        // TODO Отправлять лишь частицы из двух ближайших секций:
        //  https://github.com/Slomix/ParkourBeat/issues/17
        //noinspection UnnecessaryLocalVariable
        Iterable<Location> locations = this.particleLocations;
        ParticleUtils.displayRedstoneParticles(player, color, locations, MAX_PARTICLES_VIEW_DISTANCE_SQUARED);
    }

    public void startSpawnParticles(@NonNull Player player) {
        if (player.getWorld() != this.world) {
            throw new IllegalStateException(
                "Player is not in world " + this.world.getName() + "!\nPlayer world: " + player.getWorld().getName());
        }

        this.particleViewers.add(player);
    }

    public void stopSpawnParticlesForPlayer(@NonNull Player player) {
        this.particleViewers.remove(player);
    }

    public void stopSpawnParticles() {
        this.plugin.get(LevelsManager.class).removeParticleController(this);
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    @NonNull
    private Color getCurrentColor(@NonNull Location location) {
        Color lastColor = null;
        for (Map.Entry<Double, Color> entry : this.colorsChangeLocations.entrySet()) {
            if (lastColor == null || this.directionChecker.isAheadDirection(location, entry.getKey())) {
                lastColor = entry.getValue();
                continue;
            }
            break;
        }
        return lastColor == null ? EditTrackPointsItem.DEFAULT_PARTICLES_COLOR : lastColor;
    }

    @NonNull
    private List<Location> createStraightPath(@NonNull Location start, @NonNull Location end) {
        List<Location> path = new ArrayList<>();
        Vector vector = end.toVector().subtract(start.toVector());
        double length = vector.length();
        vector.normalize();

        double points = length * 4;

        for (double i = 0; i < length; i += length / points) {
            Location location = start.clone().add(vector.clone().multiply(i)).add(0, 0.2, 0);
            path.add(location);
        }

        return path;
    }
}
