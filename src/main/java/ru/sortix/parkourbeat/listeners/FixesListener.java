package ru.sortix.parkourbeat.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.sortix.parkourbeat.ParkourBeat;

// Source: https://github.com/kennytv/ExploitFixes
@RequiredArgsConstructor
public class FixesListener implements Listener {
    private final ParkourBeat plugin;

    @EventHandler
    private void on(AsyncTabCompleteEvent event) {
        if (event.getBuffer().length() <= 256) return;
        if (!(event.getSender() instanceof Player)) return;
        event.setCancelled(true);
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> ((Player) event.getSender())
                .banPlayer("Использование стороннего ПО"));
    }
}
