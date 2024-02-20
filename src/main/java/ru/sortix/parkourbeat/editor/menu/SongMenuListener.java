package ru.sortix.parkourbeat.editor.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class SongMenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof SongMenu)) {
            return;
        }
        event.setCancelled(true);
        SongMenu menu = (SongMenu) event.getClickedInventory().getHolder();
        menu.onClick(event.getSlot());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() instanceof SongMenu) {
            event.setCancelled(true);
        }
    }
}
