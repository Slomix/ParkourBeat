package ru.sortix.parkourbeat.player.packet.bundle;

import com.comphenix.protocol.events.PacketContainer;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.List;

public interface BundlePacketsSender {
    void inject(@NonNull Player player);

    void uninject(@NonNull Player player);

    void sendPackets(@NonNull Player player, @NonNull List<PacketContainer> packets);
}
