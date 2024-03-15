package ru.sortix.parkourbeat.inventory;

import lombok.NonNull;
import ru.sortix.parkourbeat.ParkourBeat;

public abstract class ParkourBeatInventory extends PluginInventory<ParkourBeat> {
    public ParkourBeatInventory(@NonNull ParkourBeat plugin) {
        super(plugin);
    }
}
