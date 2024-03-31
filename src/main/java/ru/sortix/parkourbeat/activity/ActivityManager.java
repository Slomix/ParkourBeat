package ru.sortix.parkourbeat.activity;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.SpectateActivity;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.lifecycle.PluginManager;
import ru.sortix.parkourbeat.world.TeleportUtils;

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
    public Collection<UserActivity> getAllActivities() { return this.activities.values(); }

    private void setActivity(@NonNull Player player, @Nullable UserActivity newActivity) {
        UserActivity previousActivity = this.activities.get(player);

        if (previousActivity == newActivity) return;

        if (previousActivity != null) {
            try {
                previousActivity.endActivity();
            } catch (Exception e) {
                this.plugin.getLogger().log(java.util.logging.Level.SEVERE,
                    "Unable to end activity " + previousActivity.getClass().getSimpleName()
                        + " of player " + player.getName(), e);
                return;
            }
            this.activities.remove(player);
        }

        if (newActivity != null) {
            try {
                newActivity.startActivity();
            } catch (Exception e) {
                this.plugin.getLogger().log(java.util.logging.Level.SEVERE,
                    "Unable to start activity " + newActivity.getClass().getSimpleName()
                        + " of player " + player.getName(), e);
                return;
            }
            this.activities.put(player, newActivity);
        }
    }

    @NonNull
    public CompletableFuture<Boolean> switchActivity(@NonNull Player player,
                                                     @Nullable UserActivity newActivity,
                                                     @Nullable Location targetLocation
    ) {
        UserActivity previousActivity = this.getActivity(player);

        this.setActivity(player, newActivity);

        if (targetLocation == null) {
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        TeleportUtils.teleportAsync(this.plugin, player, targetLocation).thenAccept(success -> {
            if (!success) {
                this.setActivity(player, previousActivity);
            }
            result.complete(success);
        });

        return result;
    }

    protected void updateTargetLocationActivity(@NonNull Player player, @NonNull World targetWorld) {
        Level targetLevel = this.plugin.get(LevelsManager.class).getLoadedLevel(targetWorld);

        if (targetLevel == null) {
            this.setActivity(player, null);
        } else {
            UserActivity previousActivity = this.getActivity(player);
            if (previousActivity == null || !previousActivity.isValidWorld(targetWorld)) {
                this.setActivity(player, new SpectateActivity(this.plugin, player, targetLevel));
            }
        }
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
