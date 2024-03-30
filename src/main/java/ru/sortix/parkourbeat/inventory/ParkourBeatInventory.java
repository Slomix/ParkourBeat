package ru.sortix.parkourbeat.inventory;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import ru.sortix.parkourbeat.ParkourBeat;

public abstract class ParkourBeatInventory extends PluginInventory<ParkourBeat> {
    protected ParkourBeatInventory(@NonNull ParkourBeat plugin, int rows, @NonNull Component title) {
        super(plugin, rows, title);
    }

    protected ParkourBeatInventory(@NonNull ParkourBeat plugin, @NonNull InventoryType type, @NonNull Component title) {
        super(plugin, type, title);
    }
}
