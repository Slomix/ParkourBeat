package ru.sortix.parkourbeat.player.packet.bundle;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.drupaldoesnotexists.bundlelib.CrossBundleLib;
import com.drupaldoesnotexists.bundlelib.adapter.Bundle;
import com.drupaldoesnotexists.bundlelib.impl.v1_16.BundleLibAdapter16;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BundleLibPacketsSender implements BundlePacketsSender {
    private final Logger logger;
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final CrossBundleLib lib = new CrossBundleLib(new BundleLibAdapter16());

    @Override
    public void inject(@NonNull Player player) {
        try {
            this.lib.inject(player);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "[CrossBundleLib] " +
                "Unable to inject for player " + player.getName(), e);
        }
    }

    @Override
    public void uninject(@NonNull Player player) {
    }

    @Override
    public void sendPackets(@NonNull Player player, @NonNull List<PacketContainer> packets) {
        List<Object> packetHandles = packets.stream().map(PacketContainer::getHandle).toList();

        Bundle<Object> bundle;
        try {
            bundle = this.lib.createBundle(packetHandles);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "[CrossBundleLib] " +
                "Unable to create bundle for player " + player.getName() + " with packets: "
                + packetHandles.stream().map(o -> o.getClass().getSimpleName())
                .collect(Collectors.joining(";")), e);
            return;
        }

        Object bundlePacket = bundle.asPacket();
        PacketType packetType = packets.iterator().next().getType();
        this.protocolManager.sendServerPacket(player, new PacketContainer(packetType, bundlePacket));
    }
}
