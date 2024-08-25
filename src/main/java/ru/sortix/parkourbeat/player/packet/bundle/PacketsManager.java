package ru.sortix.parkourbeat.player.packet.bundle;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.lifecycle.PluginManager;

@Getter
public final class PacketsManager implements PluginManager, Listener {
    private static final boolean USE_CROSS_BUNDLE_LIB = false;

    private final Server server;
    private final BundlePacketsSender bundlePacketsSender;

    public PacketsManager(@NonNull ParkourBeat plugin) {
        this.server = plugin.getServer();
        // TODO Test if specific plugins are installed and enabled (ProtocolLib & CrossBundleLib)
        this.bundlePacketsSender = USE_CROSS_BUNDLE_LIB
            ? new BundleLibPacketsSender(plugin.getLogger())
            : new ProtocolLibPacketsSender();

        this.injectAll();
        this.server.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        this.uninjectAll();
    }

    private void injectAll() {
        for (Player player : this.server.getOnlinePlayers()) {
            this.bundlePacketsSender.inject(player);
        }
    }

    private void uninjectAll() {
        for (Player player : this.server.getOnlinePlayers()) {
            this.bundlePacketsSender.uninject(player);
        }
    }

    @EventHandler
    private void on(PlayerJoinEvent event) {
        this.bundlePacketsSender.inject(event.getPlayer());
    }

    @EventHandler
    private void on(PlayerQuitEvent event) {
        this.bundlePacketsSender.uninject(event.getPlayer());
    }
}
