package ru.sortix.parkourbeat.activity;

import lombok.NonNull;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.lifecycle.PluginManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ActivityManager implements PluginManager {
    private final ParkourBeat plugin;
    private final ActivityListener listener;
    private final Map<Player, UserActivity> activities = new HashMap<>();
    private final BukkitTask movementController;

    public ActivityManager(@NonNull ParkourBeat plugin) {
        this.plugin = plugin;
        this.listener = new ActivityListener(this);
        Server server = this.plugin.getServer();
        server.getPluginManager().registerEvents(this.listener, this.plugin);
        this.movementController = server.getScheduler()
            .runTaskTimer(
                this.plugin,
                () -> {
                    for (UserActivity activity : this.activities.values()) {
                        activity.onTick();
                    }
                },
                1L,
                1L);
    }

    @Override
    public void disable() {
        this.movementController.cancel();
        HandlerList.unregisterAll(this.listener);
        for (Player player : new HashSet<>(this.activities.keySet())) {
            this.setActivity(player, null);
        }
    }

    @Nullable
    public UserActivity getActivity(@NonNull Player player) {
        return this.activities.get(player);
    }

    @NonNull
    public CompletableFuture<Void> setActivity(@NonNull Player player, @Nullable UserActivity activity) {
        UserActivity previousActivity = this.activities.get(player);

        if (previousActivity == activity) return CompletableFuture.completedFuture(null);

        CompletableFuture<Void> result = new CompletableFuture<>();
        CompletableFuture<Void> endActivity = previousActivity == null ? CompletableFuture.completedFuture(null) : previousActivity.endActivity();
        endActivity.thenAccept(unused -> {
            if (activity == null) {
                this.activities.remove(player);
                result.complete(null);
            } else {
                this.activities.put(player, activity);
                activity.startActivity().thenAccept(unused1 -> result.complete(null));
            }
        });
        return result;
    }

    protected void updateActivityWorld(@NonNull Player player, @NonNull World newWorld) {
        UserActivity previousActivity = this.activities.get(player);
        if (previousActivity == null) return;
        if (previousActivity.isValidWorld(newWorld)) return;
        this.setActivity(player, null);
    }

    @NonNull
    public Collection<Player> getPlayersOnTheLevel(@NonNull Level level) {
        Collection<Player> result = new ArrayList<>();
        for (Player player : level.getWorld().getPlayers()) {
            UserActivity activity = this.getActivity(player);
            if (activity != null && activity.getLevel() == level) result.add(player);
        }
        return result;
    }
}
