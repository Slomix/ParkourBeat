package ru.sortix.parkourbeat.player.input;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.player.input.type.ChatInput;
import ru.sortix.parkourbeat.player.input.type.PlayerInput;
import ru.sortix.parkourbeat.player.input.type.PlayerInputType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlayersInputManager implements PluginManager, Listener {
    private final ParkourBeat plugin;
    private final Map<Player, InputRequest> requestedPlayers = new HashMap<>();

    public PlayersInputManager(@NonNull ParkourBeat plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public boolean isInputRequested(@NonNull Player player) {
        return this.requestedPlayers.containsKey(player);
    }

    @NonNull
    public CompletableFuture<String> requestChatInput(@NonNull Player player, int timeoutTicks) {
        return this.requestInput(PlayerInputType.CHAT, player, timeoutTicks)
            .thenApply(playerInput -> playerInput == null ? null : ((ChatInput) playerInput).getMessage());
    }

    @NonNull
    private CompletableFuture<PlayerInput> requestInput(
        @NonNull PlayerInputType type, @NonNull Player player, int timeoutTicks) {
        if (this.requestedPlayers.containsKey(player)) {
            throw new IllegalStateException("Already waiting input from player " + player.getName());
        }

        CompletableFuture<PlayerInput> future = new CompletableFuture<>();
        this.requestedPlayers.put(player, new InputRequest(type, future));
        this.plugin
            .getServer()
            .getScheduler()
            .runTaskLaterAsynchronously(
                this.plugin,
                () -> {
                    if (!future.isDone()) {
                        this.requestedPlayers.remove(player);
                        future.complete(null);
                    }
                },
                timeoutTicks);
        return future;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void on(AsyncChatEvent event) {
        InputRequest request = this.requestedPlayers.remove(event.getPlayer());
        if (request == null || request.type != PlayerInputType.CHAT) return;
        event.setCancelled(true);
        String message = PlainComponentSerializer.plain().serialize(event.message());
        request.future.complete(new ChatInput(message));
    }

    @EventHandler
    private void on(PlayerQuitEvent event) {
        InputRequest request = this.requestedPlayers.remove(event.getPlayer());
        if (request == null) return;
        request.future.complete(null);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        for (InputRequest request : this.requestedPlayers.values()) {
            request.future.complete(null);
        }
        this.requestedPlayers.clear();
    }

    @RequiredArgsConstructor
    private static final class InputRequest {
        private final @NonNull PlayerInputType type;
        private final @NonNull CompletableFuture<PlayerInput> future;
    }
}
