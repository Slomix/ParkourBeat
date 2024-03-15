package ru.sortix.parkourbeat.item.editor.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.item.editor.EditorItem;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Waypoint;

public class EditTrackParticleItem extends EditorItem {
    public static final Color DEFAULT_PARTICLES_COLOR = Color.LIME;

    public static final double MIN_DISTANCE_BETWEEN_POINTS = 0.5;
    public static final double HEIGHT_CHANGE_VALUE = 0.5;
    public static final int REMOVE_POINT_DISTANCE = 1;

    @SuppressWarnings("deprecation")
    public EditTrackParticleItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, newStack(Material.BLAZE_ROD, (meta) -> {
            meta.setDisplayName("Путь");
            meta.setLore(new ArrayList<>(Arrays.asList(
                    "ПКМ - установить точку", "ЛКМ - удалить точку", "SHIFT + ПКМ/ЛКМ - управление высотой")));
        }));
    }

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Level level = activity.getLevel();

        AtomicBoolean isChanged = new AtomicBoolean(false);
        List<Waypoint> waypoints = level.getLevelSettings().getWorldSettings().getWaypoints();

        // Обработка точек
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            Location interactionPoint = getInteractionPoint(event);
            if (interactionPoint == null) {
                return;
            }

            DirectionChecker directionChecker = level.getLevelSettings().getDirectionChecker();

            // Обработка удаления точки
            if (action == Action.LEFT_CLICK_BLOCK) {
                double particleCoordinate = directionChecker.getCoordinate(interactionPoint);
                int nearestWaypointIndex = findNearestWaypointIndex(waypoints, particleCoordinate, directionChecker);

                if (nearestWaypointIndex != -1) {
                    removeWaypointIfCloseEnough(
                            waypoints, nearestWaypointIndex, interactionPoint, player, level, isChanged);
                }
            }
            // Обработка добавления новой точки
            else {
                Waypoint newWaypoint =
                        new Waypoint(interactionPoint, activity.getCurrentColor(), activity.getCurrentHeight());
                insertWaypointInOrder(waypoints, newWaypoint, directionChecker, isChanged, player, level);
            }
        }
        // Обработка изменения высоты сегментов
        else if (player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR)) {
            adjustWaypointHeight(action, waypoints, player, activity, isChanged);
        }

        // Обновляем частицы если были изменения
        if (isChanged.get()) {
            updateParticleController(waypoints, level.getLevelSettings().getParticleController());
        }
    }

    private int findNearestWaypointIndex(
            List<Waypoint> waypoints, double particleCoordinate, DirectionChecker directionChecker) {
        int left = 0;
        int right = waypoints.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            double midCoordinate =
                    directionChecker.getCoordinate(waypoints.get(mid).getLocation());

            if (directionChecker.isNegative()) {
                if (midCoordinate > particleCoordinate) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            } else {
                if (midCoordinate < particleCoordinate) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        return left;
    }

    private void insertWaypointInOrder(
            @NonNull List<Waypoint> waypoints,
            @NonNull Waypoint newWaypoint,
            @NonNull DirectionChecker directionChecker,
            @NonNull AtomicBoolean change,
            @NonNull Player player,
            @NonNull Level level) {
        int index = findNearestWaypointIndex(
                waypoints, directionChecker.getCoordinate(newWaypoint.getLocation()), directionChecker);

        // TODO: Not working if there are many points on the same horizontal
        for (int i = Math.max(0, index - 1); i <= Math.min(waypoints.size() - 1, index + 1); i++) {
            Waypoint waypoint = waypoints.get(i);
            if (waypoint.getLocation().distance(newWaypoint.getLocation()) < MIN_DISTANCE_BETWEEN_POINTS) {
                return;
            }
        }

        waypoints.add(index, newWaypoint);
        updateBorders(index, level);

        player.sendMessage("Вы успешно добавили точку.");
        change.set(true);
    }

    private void removeWaypointIfCloseEnough(
            @NonNull List<Waypoint> waypoints,
            int index,
            @NonNull Location particleLoc,
            @NonNull Player player,
            @NonNull Level level,
            @NonNull AtomicBoolean change) {
        // TODO: Not working if there are many points on the same horizontal
        for (int i = Math.max(0, index - 1); i <= Math.min(waypoints.size() - 1, index + 1); i++) {
            Waypoint waypoint = waypoints.get(i);
            if (waypoint.getLocation().distance(particleLoc) < REMOVE_POINT_DISTANCE) {
                if (waypoints.size() <= 2) {
                    player.sendMessage("Точек должно быть минимум две!");
                    return;
                }
                waypoints.remove(i);
                updateBorders(i, level);

                player.sendMessage("Вы успешно удалили точку.");
                change.set(true);
                break;
            }
        }
    }

    private void adjustWaypointHeight(
            @NonNull Action action,
            List<Waypoint> waypoints,
            @NonNull Player player,
            @NonNull EditActivity activity,
            AtomicBoolean change) {
        Waypoint startSegment = getLookingSegment(player, waypoints);

        if (startSegment != null) {
            if (action == Action.RIGHT_CLICK_AIR) {
                activity.setCurrentHeight(Math.min(
                        255 - startSegment.getLocation().getY(), startSegment.getHeight() + HEIGHT_CHANGE_VALUE));
            } else {
                activity.setCurrentHeight(Math.max(0, startSegment.getHeight() - HEIGHT_CHANGE_VALUE));
            }
            startSegment.setHeight(activity.getCurrentHeight());
            change.set(true);
        }
    }

    private void updateBorders(int index, @NonNull Level level) {
        WorldSettings worldSettings = level.getLevelSettings().getWorldSettings();
        if (index == 0 || index == worldSettings.getWaypoints().size() - 1) {
            worldSettings.updateBorders();
        }
    }

    private void updateParticleController(List<Waypoint> waypoints, ParticleController particleController) {
        particleController.loadParticleLocations(waypoints);
    }

    private Waypoint getLookingSegment(Player player, List<Waypoint> waypoints) {

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint startSegment = waypoints.get(i);
            Waypoint endSegment = waypoints.get(i + 1);

            if (isLookingAt(
                    player,
                    startSegment.getLocation().toVector(),
                    endSegment.getLocation().toVector())) {
                return startSegment;
            }
        }
        return null;
    }

    public boolean isLookingAt(Player player, Vector block1, Vector block2) {
        Vector toBlock1 = block1.subtract(player.getEyeLocation().toVector()).setY(0);
        Vector toBlock2 = block2.subtract(player.getEyeLocation().toVector()).setY(0);

        Vector playerDirection = player.getEyeLocation().getDirection().setY(0);

        Vector cross1 = playerDirection.getCrossProduct(toBlock1);
        Vector cross2 = playerDirection.getCrossProduct(toBlock2);

        double dot = cross1.dot(cross2);

        boolean sameHalfPlane = playerDirection.dot(toBlock1) > 0 && playerDirection.dot(toBlock2) > 0;

        return dot < 0 && sameHalfPlane;
    }

    public static final int INTERACT_BLOCK_DISTANCE = 5;

    @Nullable protected static Location getInteractionPoint(@NonNull PlayerInteractEvent event) {
        Location interactionPoint = event.getInteractionPoint();
        if (interactionPoint != null) return interactionPoint;

        Player player = event.getPlayer();
        World world = player.getWorld();
        Location eyeLocation = player.getEyeLocation();
        RayTraceResult rayTrace =
                world.rayTraceBlocks(eyeLocation, eyeLocation.getDirection(), INTERACT_BLOCK_DISTANCE);
        if (rayTrace != null) {
            interactionPoint = rayTrace.getHitPosition().toLocation(world);
        }
        return interactionPoint;
    }
}
