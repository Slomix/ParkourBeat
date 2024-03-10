package ru.sortix.parkourbeat.editor.items;

import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class LeaveEditorItem extends EditorItem {

    private static final ItemStack ITEM;
    private static final int SLOT = 8;

    static {
        ITEM = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta meta = ITEM.getItemMeta();
        meta.setDisplayName("Покинуть редактор");
        ITEM.setItemMeta(meta);
    }

    private final GameManager gameManager;
    private final LevelEditorsManager levelEditorsManager;

    public LeaveEditorItem(
            @NonNull Player player,
            @NonNull Level level,
            @NonNull GameManager gameManager,
            @NonNull LevelEditorsManager levelEditorsManager) {
        super(ITEM.clone(), SLOT, player, level);
        this.gameManager = gameManager;
        this.levelEditorsManager = levelEditorsManager;
    }

    @Override
    public void onClick(Action action, Block block, @Nullable Location interactionPoint) {
        TeleportUtils.teleportAsync(this.player, Settings.getLobbySpawn()).thenAccept(success -> {
            if (!success) return;

            this.player.setGameMode(GameMode.ADVENTURE);
            if (!this.levelEditorsManager.removeEditorSession(this.player)) {
                this.gameManager.removeGame(this.player);
            }
        });
    }
}
