package ru.sortix.parkourbeat.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

// Source: https://github.com/kennytv/ExploitFixes
public class FixesListener implements Listener {
    @EventHandler
    private void on(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player)) return;
        if (event.getBuffer().length() <= 256) return;
        event.setCancelled(true);
        ((Player) event.getSender()).banPlayer("Использование стороннего ПО");
    }
}
