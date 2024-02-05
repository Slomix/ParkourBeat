package ru.sortix.parkourbeat.levels;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.location.Waypoint;

import java.util.*;

public class ParticleController {
    private final DirectionChecker directionChecker;
    private final List<Location> particleLocations = new ArrayList<>();
    private final Map<Double, Color> colorsChangeLocations = new LinkedHashMap<>();
    private final Map<Player, BukkitTask> particleTasks = new HashMap<>();
    private static final double SEGMENT_LENGTH = 0.25;
    private Iterator<Map.Entry<Double, Color>> colorIterator;
    private Map.Entry<Double, Color> currentEntry;
    private boolean isLoaded = false;

    public ParticleController(DirectionChecker directionChecker) {
        this.directionChecker = directionChecker;
    }

    public void loadParticleLocations(ArrayList<Waypoint> waypoints) {
        Color previousColor = null;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint currentPoint = waypoints.get(i);
            Waypoint nextPoint = waypoints.get(i + 1);
            if (!currentPoint.getColor().equals(previousColor)) {
                colorsChangeLocations.put(directionChecker.getCoordinate(currentPoint.getLocation()), currentPoint.getColor());
                previousColor = currentPoint.getColor();
            }

            double height = currentPoint.getHeight();
            if (height == 0) {
                List<Location> straightPath = createStraightPath(currentPoint.getLocation(), nextPoint.getLocation());
                particleLocations.addAll(straightPath);
            } else {
                List<Location> curvedPath = createCurvedPath(currentPoint.getLocation(), nextPoint.getLocation(), height);
                particleLocations.addAll(curvedPath);
            }
        }

        isLoaded = true;
    }

    public void startSpawnParticles(Player player) {
        particleTasks.put(player, Bukkit.getScheduler().runTaskTimer(ParkourBeat.getPlugin(), () -> {
            for (Location location : particleLocations) {
                Color color = getCurrentColor(player.getLocation());
                player.spawnParticle(Particle.REDSTONE, location, 0, color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, 1);
            }
        }, 0, 20));
    }

    public void stopSpawnParticles(Player player) {
        BukkitTask task = particleTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    private Color getCurrentColor(Location location) {
        if (colorIterator == null) {
            colorIterator = colorsChangeLocations.entrySet().iterator();
        }
        if (currentEntry == null || directionChecker.isAheadDirection(location, currentEntry.getKey())) {
            if (currentEntry == null || colorIterator.hasNext())
                currentEntry = colorIterator.next();
        }
        return currentEntry.getValue();
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

            Vector interpolated = cubicBezierInterpolation(startVector, control1, control2, endVector, ratio);

            Location location = new Location(start.getWorld(), interpolated.getX(), interpolated.getY(), interpolated.getZ());
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

    private static Vector cubicBezierInterpolation(Vector p0, Vector p1, Vector p2, Vector p3, double t) {
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


}
