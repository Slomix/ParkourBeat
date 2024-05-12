package ru.sortix.parkourbeat.activity;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
public class ActivityListener implements Listener {
    private final ActivityManager manager;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() == event.getTo().getWorld()) return;
        this.manager.updateTargetLocationActivity(event.getPlayer(), event.getTo().getWorld());
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        this.manager.switchActivity(event.getPlayer(), null, null);
        this.manager.getPacketsAdapter().onPlayerQuit(event.getPlayer());
    }
}
