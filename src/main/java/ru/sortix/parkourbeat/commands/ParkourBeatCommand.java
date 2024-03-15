package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import ru.sortix.parkourbeat.ParkourBeat;

public abstract class ParkourBeatCommand extends PluginCommand<ParkourBeat> {
    public ParkourBeatCommand(@NonNull ParkourBeat plugin) {
        super(plugin);
    }
}
