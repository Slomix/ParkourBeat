package ru.sortix.parkourbeat.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.inventory.event.ClickEvent;

public abstract class PluginInventory<P extends JavaPlugin> implements InventoryHolder {
    protected final @NonNull P plugin;
    private final Inventory handle;
    private final Map<Integer, Consumer<ClickEvent>> clickActions = new HashMap<>();

    protected PluginInventory(@NonNull P plugin, int rows, @NonNull String title) {
        this.plugin = plugin;
        //noinspection deprecation
        this.handle = plugin.getServer().createInventory(this, rows * 9, title);
    }

    protected PluginInventory(@NonNull P plugin, @NonNull InventoryType type, @NonNull String title) {
        this.plugin = plugin;
        //noinspection deprecation
        this.handle = plugin.getServer().createInventory(this, type, title);
    }

    protected void setItem(int row, int column, @Nullable ItemStack stack, @Nullable Consumer<ClickEvent> action) {
        int slot = ((row - 1) * 9) + (column - 1);
        this.setItem(slot, stack, action);
    }

    protected void setItem(int slotIndex, @Nullable ItemStack stack, @Nullable Consumer<ClickEvent> action) {
        this.handle.setItem(slotIndex, stack);
        if (stack == null) {
            if (action == null) this.clickActions.remove(slotIndex);
            else throw new IllegalArgumentException("Action must be null with null item");
        } else {
            if (action == null) this.clickActions.remove(slotIndex);
            else this.clickActions.put(slotIndex, action);
        }
    }

    protected void clearInventory() {
        this.handle.clear();
        this.clickActions.clear();
    }

    public void open(@NonNull Player player) {
        player.openInventory(this.handle);
    }

    protected final void handle(@NonNull InventoryClickEvent event) {
        event.setCancelled(true);
        Consumer<ClickEvent> action = this.clickActions.get(event.getRawSlot());
        if (action == null) return;
        ClickEvent clickEvent = ClickEvent.newInstance(event);
        if (clickEvent == null) return;
        try {
            action.accept(clickEvent);
        } catch (Exception e) {
            this.plugin
                    .getLogger()
                    .log(
                            Level.SEVERE,
                            "Unable to handle action of player "
                                    + event.getWhoClicked().getName() + " in inventory "
                                    + this.getClass().getName() + " with raw slot index " + event.getRawSlot(),
                            e);
        }
    }

    protected final void handle(@NonNull InventoryDragEvent event) {
        event.setCancelled(true);
    }

    protected final void handle(@NonNull InventoryCloseEvent event) {
        this.onClose((Player) event.getPlayer());
    }

    protected void onClose(@NonNull Player player) {}

    @Override
    public final @NotNull Inventory getInventory() {
        return this.handle;
    }
}
