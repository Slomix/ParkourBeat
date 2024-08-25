package ru.sortix.parkourbeat.activity;

import com.comphenix.protocol.events.PacketContainer;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.List;

public interface ActivityPacketsAdapter {
    void setWatchingPosition(@NonNull Player player, boolean watching);

    @NonNull
    Vector getPosition(@NonNull Player player);

    void startWatchingSoundPacketsBundle(@NonNull Player player,
                                         @NonNull Consumer<List<PacketContainer>> onPacketsFormed
    );
}
