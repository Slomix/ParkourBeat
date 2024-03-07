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
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class TestItem extends EditorItem {

    private static final ItemStack ITEM;
    private static final int SLOT = 6;

    static {
        ITEM = new ItemStack(Material.DIAMOND);
        ItemMeta meta = ITEM.getItemMeta();
        meta.setDisplayName("Протестировать уровень");
        ITEM.setItemMeta(meta);
    }

    private final GameManager gameManager;
    private final ItemsContainer itemsContainer;

    public TestItem(
            @NonNull Player player,
            @NonNull Level level,
            @NonNull GameManager gameManager,
            @NonNull ItemsContainer itemsContainer) {
        super(ITEM.clone(), SLOT, player, level);
        this.gameManager = gameManager;
        this.itemsContainer = itemsContainer;
    }

    @Override
    public void onClick(Action action, Block block, @Nullable Location interactionPoint) {
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (gameManager.isInGame(player)) {
                gameManager.removeGame(player, false);
                TeleportUtils.teleport(
                        player, level.getLevelSettings().getWorldSettings().getSpawn());
                player.setGameMode(GameMode.CREATIVE);
                itemsContainer.giveToPlayer();
                level.getLevelSettings().getParticleController().startSpawnParticles(player);
                player.sendMessage("Вы вышли из режима тестирования");
            } else {
                level.getLevelSettings().getParticleController().stopSpawnParticlesForPlayer(player);
                player.sendMessage("Загрузка уровня...");
                gameManager.createNewGame(player, level.getLevelId()).thenAccept(unused -> {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.getInventory().clear();

                    ItemStack newItem = ITEM.clone();
                    itemsContainer.updateEditorItem(itemStack, newItem);
                    itemStack = newItem;

                    player.getInventory().setItem(SLOT, itemStack);
                    player.sendMessage("Вы вошли в режим тестирования");
                });
            }
        }
    }
}
