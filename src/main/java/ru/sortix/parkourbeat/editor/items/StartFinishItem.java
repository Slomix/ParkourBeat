package ru.sortix.parkourbeat.editor.items;

import java.util.ArrayList;
import java.util.Arrays;
import lombok.NonNull;
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
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class StartFinishItem extends EditorItem {

    private static final ItemStack ITEM;
    private static final int SLOT = 4;

    static {
        ITEM = new ItemStack(Material.STICK);
        ItemMeta meta = ITEM.getItemMeta();
        meta.setDisplayName("Зона старта/финиша");
        meta.setLore(
                new ArrayList<>(
                        Arrays.asList("ЛКМ - установить зону старта", "ПКМ - установить зону финиша")));
        ITEM.setItemMeta(meta);
    }

    public StartFinishItem(@NonNull Player player, @NonNull Level level) {
        super(ITEM.clone(), SLOT, player, level);
    }

    @Override
    public void onClick(Action action, Block block, @Nullable Location interactionPoint) {
        if (interactionPoint == null) {
            return;
        }
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Vector finishPoint = interactionPoint.toVector();
            updateRegion(finishPoint, false);
            player.sendMessage(
                    "Finish border at: " + level.getLevelSettings().getWorldSettings().getFinishBorder());
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            Vector startPoint = interactionPoint.toVector();
            updateRegion(startPoint, true);
            player.sendMessage(
                    "Start border at: " + level.getLevelSettings().getWorldSettings().getStartBorder());
        }
    }

    private void updateRegion(Vector point, boolean isStart) {
        LevelSettings levelSettings = level.getLevelSettings();
        WorldSettings worldSettings = levelSettings.getWorldSettings();

        if (isStart) {
            worldSettings.setStartBorder(point);
        } else {
            worldSettings.setFinishBorder(point);
        }

        DirectionChecker.Direction previousDirection =
                levelSettings.getDirectionChecker().getDirection();
        levelSettings.updateDirectionChecker();
        DirectionChecker.Direction newDirection = levelSettings.getDirectionChecker().getDirection();
        DirectionChecker directionChecker = levelSettings.getDirectionChecker();

        worldSettings.updateEndWaypoints(directionChecker);

        if (previousDirection != newDirection) {
            worldSettings.sortWaypoints(directionChecker);
        }

        levelSettings.getParticleController().loadParticleLocations(worldSettings.getWaypoints());
    }
}
