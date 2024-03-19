package ru.sortix.parkourbeat.inventory;

import java.util.function.BiConsumer;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.ParkourBeat;

public class InventoriesListener implements Listener {
    private static final BiConsumer<PluginInventory<?>, InventoryClickEvent> handlerInventoryClickEvent =
            PluginInventory::handle;
    private static final BiConsumer<PluginInventory<?>, InventoryDragEvent> handlerInventoryDragEvent =
            PluginInventory::handle;
    private static final BiConsumer<PluginInventory<?>, InventoryCloseEvent> handlerInventoryCloseEvent =
            PluginInventory::handle;

    private final Plugin plugin;

    public InventoriesListener(@NonNull ParkourBeat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void on(InventoryClickEvent event) {
        this.handleEvent(event, handlerInventoryClickEvent);
    }

    @EventHandler
    private void on(InventoryDragEvent event) {
        this.handleEvent(event, handlerInventoryDragEvent);
    }

    @EventHandler
    private void on(InventoryCloseEvent event) {
        this.handleEvent(event, handlerInventoryCloseEvent);
    }

    private <E extends InventoryEvent> void handleEvent(
            @NonNull E event, @NonNull BiConsumer<PluginInventory<?>, E> handler) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof PluginInventory<?>)) return;
        handler.accept((PluginInventory<?>) inventory.getHolder(), event);
    }

    @EventHandler
    private void on(PluginDisableEvent event) {
        if (event.getPlugin() != this.plugin) return;
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory<?>) {
                player.closeInventory();
            }
        }
    }
}
