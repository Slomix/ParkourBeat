package ru.sortix.parkourbeat.inventory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public abstract class PluginInventory<P extends JavaPlugin> implements InventoryHolder {
    protected final @NonNull P plugin;

    public final void open(@NonNull Player player) {
        player.openInventory(this.getInventory());
    }

    public abstract void onClick(@NonNull Player player, int slot);

    public abstract void onClose(@NonNull Player player);
}
