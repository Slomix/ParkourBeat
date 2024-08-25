package ru.sortix.parkourbeat.player.packet.bundle;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.List;

public class ProtocolLibPacketsSender implements BundlePacketsSender {
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    @Override
    public void inject(@NonNull Player player) {
    }

    @Override
    public void uninject(@NonNull Player player) {
    }

    @Override
    public void sendPackets(@NonNull Player player, @NonNull List<PacketContainer> packets) {
        for (PacketContainer packet : packets) {
            this.protocolManager.sendServerPacket(player, packet);
        }
    }
}
