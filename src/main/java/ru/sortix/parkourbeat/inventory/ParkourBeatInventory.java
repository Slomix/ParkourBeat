package ru.sortix.parkourbeat.inventory;

import lombok.NonNull;
import org.bukkit.event.inventory.InventoryType;
import ru.sortix.parkourbeat.ParkourBeat;

public abstract class ParkourBeatInventory extends PluginInventory<ParkourBeat> {
    protected ParkourBeatInventory(@NonNull ParkourBeat plugin, int rows, @NonNull String title) {
        super(plugin, rows, title);
    }

    protected ParkourBeatInventory(@NonNull ParkourBeat plugin, @NonNull InventoryType type, @NonNull String title) {
        super(plugin, type, title);
    }
}
