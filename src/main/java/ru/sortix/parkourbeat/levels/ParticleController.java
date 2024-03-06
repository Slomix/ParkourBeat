package ru.sortix.parkourbeat.levels;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.editor.items.ParticleItem;
import ru.sortix.parkourbeat.location.Waypoint;
import ru.sortix.parkourbeat.utils.java.ParticleUtils;

public class ParticleController {
    private static final double SEGMENT_LENGTH = 0.25;
    private static final double MAX_PARTICLES_VIEW_DISTANCE_SQUARED = Math.pow(10, 2);

    private final Plugin plugin;
    private final ConcurrentLinkedQueue<Location> particleLocations = new ConcurrentLinkedQueue<>();
    private final Map<Double, Color> colorsChangeLocations = new LinkedHashMap<>();
    private final Set<Player> particleViewers = ConcurrentHashMap.newKeySet();
    @Setter private DirectionChecker directionChecker;
    private final World world;
    private BukkitTask particleTask = null;
    private boolean isLoaded = false;

    public ParticleController(Plugin plugin, World world, DirectionChecker directionChecker) {
        this.plugin = plugin;
        this.world = world;
        this.directionChecker = directionChecker;
    }

    public static List<Location> createCurvedPath(Location start, Location end, double height) {
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

            Vector interpolated =
                    cubicBezierInterpolation(startVector, control1, control2, endVector, ratio);

            Location location =
                    new Location(
                            start.getWorld(), interpolated.getX(), interpolated.getY(), interpolated.getZ());
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

    private static Vector cubicBezierInterpolation(
            Vector p0, Vector p1, Vector p2, Vector p3, double t) {
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

    public void loadParticleLocations(List<Waypoint> waypoints) {
        if (directionChecker == null || waypoints == null) {
            return;
        }
        if (isLoaded) {
            particleLocations.clear();
            colorsChangeLocations.clear();
            particleTask.cancel();
        }

        Color previousColor = null;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint currentPoint = waypoints.get(i);
            Waypoint nextPoint = waypoints.get(i + 1);
            if (!currentPoint.getColor().equals(previousColor)) {
                colorsChangeLocations.put(
                        directionChecker.getCoordinate(currentPoint.getLocation()), currentPoint.getColor());
                previousColor = currentPoint.getColor();
            }

            double height = currentPoint.getHeight();
            if (height == 0) {
                List<Location> straightPath =
                        createStraightPath(currentPoint.getLocation(), nextPoint.getLocation());
                particleLocations.addAll(straightPath);
            } else {
                List<Location> curvedPath =
                        createCurvedPath(currentPoint.getLocation(), nextPoint.getLocation(), height);
                particleLocations.addAll(curvedPath);
            }
        }
        particleTask =
                this.plugin
                        .getServer()
                        .getScheduler()
                        .runTaskTimerAsynchronously(
                                this.plugin,
                                () -> {
                                    if (world == null) {
                                        particleTask.cancel();
                                        return;
                                    }
                                    particleViewers.forEach(
                                            (player) -> {
                                                if (player == null || !player.isOnline()) {
                                                    throw new NullPointerException("Player is not online!");
                                                } else if (player.getWorld() != world) {
                                                    throw new IllegalStateException(
                                                            "Player is not in world "
                                                                    + world.getName()
                                                                    + "!\nPlayer world: "
                                                                    + player.getWorld());
                                                } else {
                                                    updatePlayerParticles(player);
                                                }
                                            });
                                },
                                0,
                                5);
        isLoaded = true;
    }

    public void startSpawnParticles(Player player) {
        if (player.getWorld() != world) {
            throw new IllegalStateException(
                    "Player is not in world " + world.getName() + "!\nPlayer world: " + player.getWorld());
        }
        if (particleTask == null || particleTask.isCancelled()) {
            throw new IllegalStateException("Particle task is not running!");
        }

        particleViewers.add(player);
    }

    private void updatePlayerParticles(Player player) {
        Color color = getCurrentColor(player.getLocation());

        // TODO Отправлять лишь частицы из двух ближайших секций:
        //  https://github.com/Slomix/ParkourBeat/issues/17
        Iterable<Location> locations = this.particleLocations;
        ParticleUtils.displayRedstoneParticles(
                player, color, locations, MAX_PARTICLES_VIEW_DISTANCE_SQUARED);
    }

    public void stopSpawnParticlesForPlayer(Player player) {
        particleViewers.remove(player);
    }

    public void stopSpawnParticles() {
        particleTask.cancel();
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @NonNull private Color getCurrentColor(@NonNull Location location) {
        Color lastColor = null;
        for (Map.Entry<Double, Color> entry : colorsChangeLocations.entrySet()) {
            if (lastColor == null || directionChecker.isAheadDirection(location, entry.getKey())) {
                lastColor = entry.getValue();
                continue;
            }
            break;
        }
        return lastColor == null ? ParticleItem.DEFAULT_PARTICLES_COLOR : lastColor;
    }

    private List<Location> createStraightPath(Location start, Location end) {
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
