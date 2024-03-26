package ru.sortix.parkourbeat.item.editor.type;

import lombok.NonNull;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.item.editor.EditorItem;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.Waypoint;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class EditTrackPointsItem extends EditorItem {
    public static final Color DEFAULT_PARTICLES_COLOR = Color.LIME;

    public static final double MIN_DISTANCE_BETWEEN_POINTS = 0.5;
    public static final double HEIGHT_CHANGE_VALUE = 0.5;
    public static final int REMOVE_POINT_DISTANCE = 1;
    public static final int INTERACT_BLOCK_DISTANCE = 5;

    @SuppressWarnings("deprecation")
    public EditTrackPointsItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, 0, ItemUtils.create(Material.BLAZE_ROD, (meta) -> {
            meta.setDisplayName(ChatColor.GOLD + "Путь (см. описание)");
            meta.setLore(Arrays.asList(
                ChatColor.YELLOW + "ЛКМ - установить точку",
                ChatColor.YELLOW + "ПКМ - удалить точку",
                ChatColor.YELLOW + "SHIFT + ЛКМ - увеличить высоту прыжка",
                ChatColor.YELLOW + "SHIFT + ПКМ - уменьшить высоту прыжка"));
        }));
    }

    public static void clearAllPoints(@NonNull Level level) {
        WorldSettings worldSettings = level.getLevelSettings().getWorldSettings();
        worldSettings.getWaypoints().clear();
        worldSettings.addStartAndFinishPoints(level.getWorld());
        worldSettings.updateBorders();
        level.getLevelSettings().updateParticleLocations();
    }

    private static int findNearestWaypointIndex(
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

    private static boolean insertWaypointInOrder(
        @NonNull List<Waypoint> waypoints,
        @NonNull Waypoint newWaypoint,
        @NonNull DirectionChecker directionChecker,
        @NonNull Player player,
        @NonNull Level level) {
        int index = findNearestWaypointIndex(
            waypoints, directionChecker.getCoordinate(newWaypoint.getLocation()), directionChecker);

        // TODO: Not working if there are many points on the same horizontal
        for (int i = Math.max(0, index - 1); i <= Math.min(waypoints.size() - 1, index + 1); i++) {
            Waypoint waypoint = waypoints.get(i);
            if (waypoint.getLocation().distance(newWaypoint.getLocation()) < MIN_DISTANCE_BETWEEN_POINTS) {
                return false;
            }
        }

        waypoints.add(index, newWaypoint);
        updateBorders(index, level);

        player.sendMessage("Вы успешно добавили точку.");
        return true;
    }

    private static boolean removeWaypointIfCloseEnough(
        @NonNull List<Waypoint> waypoints,
        int index,
        @NonNull Location particleLoc,
        @NonNull Player player,
        @NonNull Level level) {
        // TODO: Not working if there are many points on the same horizontal
        for (int i = Math.max(0, index - 1); i <= Math.min(waypoints.size() - 1, index + 1); i++) {
            Waypoint waypoint = waypoints.get(i);
            if (waypoint.getLocation().distance(particleLoc) < REMOVE_POINT_DISTANCE) {
                if (waypoints.size() <= 2) {
                    player.sendMessage("Точек должно быть минимум две!");
                    return false;
                }
                waypoints.remove(i);
                updateBorders(i, level);

                player.sendMessage("Вы успешно удалили точку.");
                return true;
            }
        }
        return false;
    }

    private static boolean adjustWaypointHeight(
        boolean increase,
        @NonNull List<Waypoint> waypoints,
        @NonNull Player player,
        @NonNull EditActivity activity) {
        Waypoint startSegment = getLookingSegment(player, waypoints);

        if (startSegment == null) return false;

        if (increase) {
            activity.setCurrentHeight(
                Math.min(255 - startSegment.getLocation().getY(), startSegment.getHeight() + HEIGHT_CHANGE_VALUE));
        } else {
            activity.setCurrentHeight(Math.max(0, startSegment.getHeight() - HEIGHT_CHANGE_VALUE));
        }
        startSegment.setHeight(activity.getCurrentHeight());
        return true;
    }

    private static void updateBorders(int index, @NonNull Level level) {
        WorldSettings worldSettings = level.getLevelSettings().getWorldSettings();
        if (index == 0 || index == worldSettings.getWaypoints().size() - 1) {
            worldSettings.updateBorders();
        }
    }

    private static Waypoint getLookingSegment(@NonNull Player player, @NonNull List<Waypoint> waypoints) {

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

    public static boolean isLookingAt(@NonNull Player player, @NonNull Vector block1, @NonNull Vector block2) {
        Vector toBlock1 = block1.subtract(player.getEyeLocation().toVector()).setY(0);
        Vector toBlock2 = block2.subtract(player.getEyeLocation().toVector()).setY(0);

        Vector playerDirection = player.getEyeLocation().getDirection().setY(0);

        Vector cross1 = playerDirection.getCrossProduct(toBlock1);
        Vector cross2 = playerDirection.getCrossProduct(toBlock2);

        double dot = cross1.dot(cross2);

        boolean sameHalfPlane = playerDirection.dot(toBlock1) > 0 && playerDirection.dot(toBlock2) > 0;

        return dot < 0 && sameHalfPlane;
    }

    @Nullable
    protected static Location getInteractionPoint(@NonNull PlayerInteractEvent event) {
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

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        Player player = event.getPlayer();
        Level level = activity.getLevel();

        boolean left;
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR: {
                left = true;
                break;
            }
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR: {
                left = false;
                break;
            }
            default: {
                return;
            }
        }

        boolean isChanged = false;
        WorldSettings worldSettings = level.getLevelSettings().getWorldSettings();
        List<Waypoint> waypoints = worldSettings.getWaypoints();

        if (player.isSneaking()) {
            // Изменение высоты сегментов
            if (adjustWaypointHeight(left, waypoints, player, activity)) {
                isChanged = true;
            }
        } else {
            // Добавление и удаление точек
            Location interactionPoint = getInteractionPoint(event);
            if (interactionPoint == null) {
                return;
            }

            DirectionChecker directionChecker = level.getLevelSettings().getDirectionChecker();

            if (left) {
                // Обработка добавления новой точки
                Waypoint newWaypoint =
                    new Waypoint(interactionPoint, activity.getCurrentHeight(), activity.getCurrentColor());
                if (insertWaypointInOrder(waypoints, newWaypoint, directionChecker, player, level)) {
                    isChanged = true;
                }
            } else {
                // Обработка удаления точки
                double particleCoordinate = directionChecker.getCoordinate(interactionPoint);
                int nearestWaypointIndex = findNearestWaypointIndex(waypoints, particleCoordinate, directionChecker);

                if (nearestWaypointIndex != -1) {
                    if (removeWaypointIfCloseEnough(waypoints, nearestWaypointIndex, interactionPoint, player, level)) {
                        isChanged = true;
                    }
                }
            }
        }

        // Обновляем частицы если были изменения
        if (isChanged) {
            level.getLevelSettings().updateParticleLocations();
        }
    }
}
