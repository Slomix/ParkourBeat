package ru.sortix.parkourbeat.item;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.item.editor.type.EditTrackParticleItem;
import ru.sortix.parkourbeat.item.editor.type.EditorMenuItem;
import ru.sortix.parkourbeat.item.editor.type.TestGameItem;
import ru.sortix.parkourbeat.lifecycle.PluginManager;

public class ItemsManager implements PluginManager, Listener {
    private final Logger logger;
    private final Map<ItemStack, UsableItem> allItems;
    private final Map<Class<? extends UsableItem>, UsableItem> itemsByClass = new HashMap<>();

    public ItemsManager(@NonNull ParkourBeat plugin) {
        this.logger = plugin.getLogger();
        this.allItems = new HashMap<>();

        this.registerItems(
                new TestGameItem(plugin, 0), new EditorMenuItem(plugin, 1), new EditTrackParticleItem(plugin, 2));

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void registerItems(@NonNull UsableItem... items) {
        for (UsableItem usableItem : items) {
            ItemStack itemStack = usableItem.getItemStack();
            if (!itemStack.getType().isItem()) {
                this.logger.severe(usableItem.getClass().getName() + " is not an item");
                continue;
            }
            this.allItems.put(itemStack, usableItem);
            this.itemsByClass.put(usableItem.getClass(), usableItem);
        }
    }

    public void putItem(@NonNull Player player, @NonNull Class<? extends UsableItem> itemClass) {
        UsableItem editorItem = this.itemsByClass.get(itemClass);
        if (editorItem == null) {
            throw new IllegalArgumentException("Unable to find item by class " + itemClass.getName());
        }
        player.getInventory().setItem(editorItem.slot, editorItem.itemStack.clone());
    }

    public void putAllItems(@NonNull Player player, @NonNull Class<? extends UsableItem> itemsClass) {
        PlayerInventory inventory = player.getInventory();
        for (UsableItem item : this.allItems.values()) {
            if (!itemsClass.isInstance(item)) continue;
            inventory.setItem(item.slot, item.itemStack.clone());
        }
    }

    @EventHandler
    private void on(@NonNull PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;
        if (event.getItem() == null) return;

        UsableItem usableItem = this.allItems.get(event.getItem());
        if (usableItem == null) return;
        event.setCancelled(true);
        if (event.getPlayer().getCooldown(event.getItem().getType()) > 0) return;
        event.getPlayer().setCooldown(event.getItem().getType(), 10);

        if (event.getHand() != EquipmentSlot.HAND) return;
        usableItem.onUse(event);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }
}
