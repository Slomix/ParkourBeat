package ru.sortix.parkourbeat.editor.items;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;

public class ItemsContainer {

    private final Map<ItemStack, EditorItem> editorItems;
    private final Map<Class<? extends EditorItem>, EditorItem> editorItemsByClass = new HashMap<>();
    private final Player player;

    public ItemsContainer(
            @NonNull Player player,
            @NonNull Level level,
            @NonNull GameManager gameManager,
            @NonNull LevelEditorsManager levelEditorsManager) {
        this.player = player;
        this.editorItems = new HashMap<>();

        for (EditorItem editorItem : new EditorItem[] {
            new LeaveEditorItem(player, level, gameManager, levelEditorsManager),
            new ParticleItem(player, level),
            new SpawnItem(player, level),
            new TestItem(player, level, gameManager, this)
        }) {
            ItemStack itemStack = editorItem.getItemStack();
            if (!itemStack.getType().isItem()) {
                System.err.println(editorItem.getClass().getName() + " is not an item");
                continue;
            }
            this.editorItems.put(itemStack, editorItem);
            this.editorItemsByClass.put(editorItem.getClass(), editorItem);
        }
    }

    public void giveToPlayer() {
        for (EditorItem item : editorItems.values()) {
            player.getInventory().setItem(item.getSlot(), item.getItemStack());
        }
    }

    public Map<ItemStack, EditorItem> getEditorItems() {
        return editorItems;
    }

    public <T extends EditorItem> T getEditorItem(Class<T> editorItemClass) {
        return (T) editorItemsByClass.get(editorItemClass);
    }

    public void updateEditorItem(ItemStack prevItem, ItemStack newItem) {
        EditorItem editorItem = editorItems.remove(prevItem);
        editorItems.put(newItem, editorItem);
    }
}
