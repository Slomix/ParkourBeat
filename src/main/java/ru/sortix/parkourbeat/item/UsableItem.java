package ru.sortix.parkourbeat.item;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;

@Getter
public abstract class UsableItem {
    protected final @NonNull ParkourBeat plugin;
    final int slot;
    private final int cooldownTicks;
    final @NonNull ItemStack itemStack;

    public UsableItem(@NonNull ParkourBeat plugin, int slot, int cooldownTicks, @NonNull ItemStack itemStack) {
        this.plugin = plugin;
        this.slot = slot;
        this.cooldownTicks = cooldownTicks;
        this.itemStack = ItemUtils.fixItalic(itemStack);
    }

    protected abstract void onUse(@NonNull PlayerInteractEvent event);
}
