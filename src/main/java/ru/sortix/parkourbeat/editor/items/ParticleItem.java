package ru.sortix.parkourbeat.editor.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.levels.DirectionChecker;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.location.Waypoint;

public class ParticleItem extends EditorItem {
    public static final Color DEFAULT_PARTICLES_COLOR = Color.LIME;

    private static final ItemStack ITEM;
    private static final int SLOT = 0;
    public static final double MIN_DISTANCE_BETWEEN_POINTS = 0.5;
    public static final double HEIGHT_CHANGE_VALUE = 0.5;
    public static final int REMOVE_POINT_DISTANCE = 1;

    static {
        ITEM = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = ITEM.getItemMeta();
        meta.setDisplayName("Путь");
        meta.setLore(
                new ArrayList<>(
                        Arrays.asList(
                                "ПКМ - установить точку",
                                "ЛКМ - удалить точку",
                                "SHIFT + ПКМ/ЛКМ - управление высотой")));
        ITEM.setItemMeta(meta);
    }

    private Color currentColor;
    private double currentHeight;

    public ParticleItem(@NonNull Player player, @NonNull Level level) {
        super(ITEM.clone(), SLOT, player, level);
        currentColor = DEFAULT_PARTICLES_COLOR;
        currentHeight = 0;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    @Override
    public void onClick(Action action, Block block, @Nullable Location interactionPoint) {
        if (action == Action.PHYSICAL) {
            return;
        }

        AtomicBoolean isChanged = new AtomicBoolean(false);
        List<Waypoint> waypoints = level.getLevelSettings().getWorldSettings().getWaypoints();

        // Обработка точек
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            if (interactionPoint == null) {
                return;
            }

            DirectionChecker directionChecker = level.getLevelSettings().getDirectionChecker();

            // Обработка удаления точки
            if (action == Action.LEFT_CLICK_BLOCK) {
                double particleCoordinate = directionChecker.getCoordinate(interactionPoint);
                int nearestWaypointIndex = findNearestWaypointIndex(waypoints, particleCoordinate, directionChecker);

                if (nearestWaypointIndex != -1) {
                    removeWaypointIfCloseEnough(waypoints, nearestWaypointIndex, interactionPoint, player, isChanged);
                }
            }
            // Обработка добавления новой точки
            else {
                Waypoint newWaypoint = new Waypoint(interactionPoint, currentColor, currentHeight);
                insertWaypointInOrder(waypoints, newWaypoint, directionChecker, isChanged, player);
            }
        }
        // Обработка изменения высоты сегментов
        else if (player.isSneaking() && (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR)) {
            adjustWaypointHeight(action, waypoints, player, isChanged);
        }

        // Обновляем частицы если были изменения
        if (isChanged.get()) {
            updateParticleController(waypoints, level.getLevelSettings().getParticleController(), player);
        }
    }

    private int findNearestWaypointIndex(
            List<Waypoint> waypoints, double particleCoordinate, DirectionChecker directionChecker) {
        int left = 0;
        int right = waypoints.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            double midCoordinate = directionChecker.getCoordinate(waypoints.get(mid).getLocation());

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
            List<Waypoint> waypoints,
            Waypoint newWaypoint,
            DirectionChecker directionChecker,
            AtomicBoolean change,
            Player player) {
        int index =
                findNearestWaypointIndex(
                        waypoints, directionChecker.getCoordinate(newWaypoint.getLocation()), directionChecker);

        // TODO: Not working if there are many points on the same horizontal
        for (int i = Math.max(0, index - 1); i <= Math.min(waypoints.size() - 1, index + 1); i++) {
            Waypoint waypoint = waypoints.get(i);
            if (waypoint.getLocation().distance(newWaypoint.getLocation())
                    < MIN_DISTANCE_BETWEEN_POINTS) {
                return;
            }
        }

        if (index == waypoints.size() || index == 0) {
            player.sendMessage("Вы не можете добавить точку за границами.");
            return;
        }

        waypoints.add(index, newWaypoint);
        player.sendMessage("Вы успешно добавили точку.");
        change.set(true);
    }

    private void removeWaypointIfCloseEnough(
            List<Waypoint> waypoints,
            int index,
            Location particleLoc,
            Player player,
            AtomicBoolean change) {
        // TODO: Not working if there are many points on the same horizontal
        for (int i = Math.max(0, index - 1); i <= Math.min(waypoints.size() - 1, index + 1); i++) {
            Waypoint waypoint = waypoints.get(i);
            if (waypoint.getLocation().distance(particleLoc) < REMOVE_POINT_DISTANCE) {
                if (i == 0 || i == waypoints.size() - 1) {
                    player.sendMessage("Вы не можете удалить начальную или конечную точку.");
                    return;
                }
                waypoints.remove(i);
                player.sendMessage("Вы успешно удалили точку.");
                change.set(true);
                break;
            }
        }
    }

    private void adjustWaypointHeight(
            Action action, List<Waypoint> waypoints, Player player, AtomicBoolean change) {
        Waypoint startSegment = getLookingSegment(player, waypoints);

        if (startSegment != null) {
            if (action == Action.RIGHT_CLICK_AIR) {
                currentHeight =
                        Math.min(
                                255 - startSegment.getLocation().getY(),
                                startSegment.getHeight() + HEIGHT_CHANGE_VALUE);
            } else {
                currentHeight = Math.max(0, startSegment.getHeight() - HEIGHT_CHANGE_VALUE);
            }
            startSegment.setHeight(currentHeight);
            change.set(true);
        }
    }

    private void updateParticleController(
            List<Waypoint> waypoints, ParticleController particleController, Player player) {
        particleController.loadParticleLocations(waypoints);
    }

    private Waypoint getLookingSegment(Player player, List<Waypoint> waypoints) {

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint startSegment = waypoints.get(i);
            Waypoint endSegment = waypoints.get(i + 1);

            if (isLookingAt(
                    player, startSegment.getLocation().toVector(), endSegment.getLocation().toVector())) {
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
}
