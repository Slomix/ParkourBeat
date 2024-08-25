package ru.sortix.parkourbeat.activity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityPacketsAdapterImpl extends PacketAdapter implements ActivityPacketsAdapter {
    private static final boolean DEBUG_PACKETS = false;

    private final Map<Player, Vector> positions = new ConcurrentHashMap<>();
    private final Map<Player, PacketsHandler> soundPackets = new ConcurrentHashMap<>();

    public ActivityPacketsAdapterImpl(@NonNull Plugin plugin) {
        //noinspection deprecation
        super(plugin,

            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK,

            PacketType.Play.Server.STOP_SOUND,
            PacketType.Play.Server.CUSTOM_SOUND_EFFECT

        );
    }

    @Override
    public void setWatchingPosition(@NonNull Player player, boolean watching) {
        if (watching) {
            this.positions.put(player, player.getLocation().toVector()); // null is unsupported in concurrent impl
        } else {
            this.positions.remove(player);
        }
    }

    @Override
    @NonNull
    public Vector getPosition(@NonNull Player player) {
        Vector result = this.positions.get(player);
        if (result == null) {
            throw new IllegalStateException("No position watching enabled for " + player.getName());
        }
        return result;
    }

    @Override
    public void startWatchingSoundPacketsBundle(@NonNull Player player,
                                                @NonNull Consumer<List<PacketContainer>> onPacketsFormed
    ) {
        if (this.soundPackets.put(player, new PacketsHandler(new ArrayList<>(), onPacketsFormed)) != null) {
            throw new IllegalStateException("Sound packets watching already started for " + player.getName());
        }
    }

    protected void onPlayerQuit(@NonNull Player player) {
        this.positions.remove(player);
        this.soundPackets.remove(player);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (!this.positions.containsKey(event.getPlayer())) return;
        StructureModifier<Double> doubles = event.getPacket().getDoubles();
        this.positions.put(event.getPlayer(), new Vector(
            doubles.read(0),
            doubles.read(1),
            doubles.read(2)
        ));
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();

        if (DEBUG_PACKETS) {
            //noinspection deprecation
            if (event.getPacketType() == PacketType.Play.Server.CUSTOM_SOUND_EFFECT) {
                String sound = event.getPacket().getModifier().read(0).toString().split(":")[1];
                player.sendMessage("[DEBUG] [PACKET] " + event.getPacketType() + sound);
            } else {
                player.sendMessage("[DEBUG] [PACKET] " + event.getPacketType());
            }
        }

        //noinspection deprecation
        boolean stopWatching = event.getPacketType() == PacketType.Play.Server.CUSTOM_SOUND_EFFECT;

        PacketsHandler packetsHandler = stopWatching ? this.soundPackets.remove(player) : this.soundPackets.get(player);

        if (packetsHandler == null) return; // Sound packets not watching for now
        if (stopWatching && packetsHandler.packetsList.isEmpty()) return; // ignore < 2 packets groups size

        packetsHandler.packetsList.add(event.getPacket());
        event.setCancelled(true);

        if (stopWatching) {
            packetsHandler.onPacketsFormed.accept(packetsHandler.packetsList);
        }
    }

    private record PacketsHandler(@NonNull List<PacketContainer> packetsList,
                                  @NonNull Consumer<List<PacketContainer>> onPacketsFormed
    ) {
    }
}
