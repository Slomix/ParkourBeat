package ru.sortix.parkourbeat.activity;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class ActivityListener implements Listener {
    private final ActivityManager manager;

    @EventHandler
    public void on(PlayerChangedWorldEvent event) {
        this.manager.updateActivityWorld(event.getPlayer(), event.getPlayer().getWorld());
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        this.manager.setActivity(event.getPlayer(), null);
    }
}
