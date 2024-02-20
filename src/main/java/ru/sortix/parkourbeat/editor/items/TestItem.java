package ru.sortix.parkourbeat.editor.items;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;

public class TestItem extends EditorItem {

    private static final ItemStack testItem;
    private static final int slot = 8;

    static {
        testItem = new ItemStack(Material.DIAMOND);
        ItemMeta meta = testItem.getItemMeta();
        meta.setDisplayName("Протестировать уровень");
        testItem.setItemMeta(meta);
    }

    private final GameManager gameManager;
    private final ItemsContainer itemsContainer;

    public TestItem(
            Player player, Level level, GameManager gameManager, ItemsContainer itemsContainer) {
        super(testItem.clone(), slot, player, level);
        this.gameManager = gameManager;
        this.itemsContainer = itemsContainer;
    }

    @Override
    public void onClick(Action action, Block block) {
        if (action == Action.RIGHT_CLICK_AIR) {
            if (gameManager.isInGame(player)) {
                gameManager.removeGame(player, false);
                player.teleport(level.getLevelSettings().getWorldSettings().getSpawn());
                player.setGameMode(GameMode.CREATIVE);
                itemsContainer.giveToPlayer();
                level.getLevelSettings().getParticleController().startSpawnParticles(player);
                player.sendMessage("Вы вышли из режима тестирования");
            } else {
                level.getLevelSettings().getParticleController().stopSpawnParticles(player);
                gameManager.createNewGame(player, level.getName());
                player.setGameMode(GameMode.ADVENTURE);
                player.getInventory().clear();

                ItemStack newItem = testItem.clone();
                itemsContainer.updateEditorItem(itemStack, newItem);
                itemStack = newItem;

                player.getInventory().setItem(slot, itemStack);
                player.sendMessage("Вы вошли в режим тестирования");
            }
        }
    }
}
