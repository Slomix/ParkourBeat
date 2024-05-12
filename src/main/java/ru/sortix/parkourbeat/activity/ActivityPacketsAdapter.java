package ru.sortix.parkourbeat.activity;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public interface ActivityPacketsAdapter {
    void setWatchingPosition(@NonNull Player player, boolean watching);

    @Nullable
    Vector getPosition(@NonNull Player player);
}
