package ru.sortix.parkourbeat.activity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityPacketsAdapterImpl extends PacketAdapter implements ActivityPacketsAdapter {
    private final Map<Player, Vector> positions = new ConcurrentHashMap<>();

    public ActivityPacketsAdapterImpl(@NonNull Plugin plugin) {
        super(plugin,

            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK

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

    protected void onPlayerQuit(@NonNull Player player) {
        this.positions.remove(player);
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
}
