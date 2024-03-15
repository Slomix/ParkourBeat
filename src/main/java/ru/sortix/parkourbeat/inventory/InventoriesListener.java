package ru.sortix.parkourbeat.inventory;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.ParkourBeat;

@SuppressWarnings("CodeBlock2Expr")
public class InventoriesListener implements Listener {
    private final Plugin plugin;

    public InventoriesListener(@NonNull ParkourBeat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void on(InventoryClickEvent event) {
        this.doPluginInventoryAction(event.getInventory(), inventory -> {
            event.setCancelled(true);
            inventory.onClick(((Player) event.getWhoClicked()), event.getSlot());
        });
    }

    @EventHandler
    private void on(InventoryDragEvent event) {
        this.doPluginInventoryAction(event.getInventory(), inventory -> {
            event.setCancelled(true);
        });
    }

    @EventHandler
    private void on(InventoryCloseEvent event) {
        this.doPluginInventoryAction(event.getInventory(), inventory -> {
            inventory.onClose((Player) event.getPlayer());
        });
    }

    @EventHandler
    private void on(PluginDisableEvent event) {
        if (event.getPlugin() != this.plugin) return;
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.doPluginInventoryAction(
                    player.getOpenInventory().getTopInventory(), inventory -> player.closeInventory());
        }
    }

    private void doPluginInventoryAction(@Nullable Inventory inventory, @NonNull Consumer<PluginInventory<?>> action) {
        if (inventory == null) return;
        if (!(inventory.getHolder() instanceof PluginInventory<?>)) return;
        action.accept(((PluginInventory<?>) inventory.getHolder()));
    }
}
