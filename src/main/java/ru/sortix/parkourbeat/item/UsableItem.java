package ru.sortix.parkourbeat.item;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;

@RequiredArgsConstructor
@Getter
public abstract class UsableItem {
    protected final @NonNull ParkourBeat plugin;
    protected final int slot;
    protected final @NonNull ItemStack itemStack;

    protected abstract void onUse(@NonNull PlayerInteractEvent event);
}
