package ru.sortix.parkourbeat.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.dao.LevelSettingDAO;
import ru.sortix.parkourbeat.world.TeleportUtils;

import java.util.function.Consumer;

public final class GamesListener implements Listener {
    private final ParkourBeat plugin;
    private final ActivityManager activityManager;
    private final LevelSettingDAO levelSettingDAO;
    private final Consumer<Player> onPlayerTeleportToLobby = player -> {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(-40);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
    };
    private final ChatRenderer.ViewerUnaware viewerUnaware = new ChatRenderer.ViewerUnaware() {
        @Override
        public @NotNull Component render(
            @NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message) {
            int lvl = 0;
            TextColor nameColor =
                source.hasPermission("parkourbeat.chat.admin") ? NamedTextColor.RED : NamedTextColor.WHITE;
            return Component.text("#" + lvl + " ", NamedTextColor.GRAY)
                .append(sourceDisplayName.color(nameColor))
                .append(Component.text(" -> ", NamedTextColor.WHITE))
                .append(message.color(NamedTextColor.WHITE));
        }
    };

    public GamesListener(@NonNull ParkourBeat plugin) {
        this.plugin = plugin;
        this.activityManager = plugin.get(ActivityManager.class);
        this.levelSettingDAO =
            plugin.get(LevelsManager.class).getLevelsSettings().getLevelSettingDAO();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (this.teleportToSpawnIfInLevelWorld(player)) {
                player.sendMessage("Перезагрузка плагина привела к телепортации в лобби");
            }
        }
    }

    @EventHandler
    private void on(PlayerTeleportEvent event) {
        World from = event.getFrom().getWorld();
        World to = event.getTo().getWorld();
        if (from == to) return;

        if (true) return; // TODO Implement auto spectating

        UserActivity oldActivity = this.activityManager.getActivity(event.getPlayer());
        if (oldActivity == null) {
            event.getPlayer().sendMessage("Текущая активность не найдена");
        } else if (oldActivity.getLevel().getWorld() == from) {
            event.getPlayer().sendMessage("Завершаем старую активность (" + from.getName() + ")");
            if (false) oldActivity.endActivity();
        } else if (oldActivity.getLevel().getWorld() == to) {
            event.getPlayer().sendMessage("Запускаем новую активность (" + to.getName() + ")");
        } else {
            event.getPlayer()
                .sendMessage("Миры " + from.getName() + " и " + to.getName() + " не относятся к активностям");
        }
    }

    @EventHandler
    private void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.isLobby(player.getWorld())) {
            this.onPlayerTeleportToLobby.accept(player);
        } else {
            this.teleportToSpawnIfInLevelWorld(player);
        }
    }

    @EventHandler
    private void on(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();

        if (true) {
            event.setSpawnLocation(Settings.getLobbySpawn());
            return;
        }

        if (this.isLobby(player.getWorld())) {
            event.setSpawnLocation(Settings.getLobbySpawn());
        } else {
            this.teleportToSpawnIfInLevelWorld(player);
        }
    }

    @EventHandler
    private void on(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (this.isLobby(player.getWorld())) {
            event.setRespawnLocation(Settings.getLobbySpawn());
        } else {
            this.doActivityAction(player, activity -> {
                event.setRespawnLocation(activity.getLevel().getSpawn());
                activity.startActivity();
            });
        }
    }

    private boolean teleportToSpawnIfInLevelWorld(@NonNull Player player) {
        if (!this.levelSettingDAO.isLevelWorld(player.getWorld())) return false;
        TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn())
            .thenAccept(success -> {
                if (!success) return;
                this.onPlayerTeleportToLobby.accept(player);
            });
        return true;
    }

    @EventHandler
    private void on(PlayerQuitEvent event) {
        this.doActivityAction(event.getPlayer(), UserActivity::endActivity);
    }

    @EventHandler
    private void on(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (this.isNotInLobbyOrLevel(player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void on(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = ((Player) event.getEntity());
        if (this.isNotInLobbyOrLevel(player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void on(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (this.isNotInLobbyOrLevel(player)) return;

        event.setKeepInventory(true);
        event.getDrops().clear();
        player.spigot().respawn();

        this.doActivityAction(player, UserActivity::startActivity);
    }

    @EventHandler
    private void on(FoodLevelChangeEvent event) {
        if (this.isNotInLobbyOrLevel((Player) event.getEntity())) return;
        if (event.getFoodLevel() != 20) {
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    private void on(PlayerDropItemEvent event) {
        if (this.isNotInLobbyOrLevel(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onActivityEvent(PlayerResourcePackStatusEvent event) {
        this.doActivityAction(event.getPlayer(), activity -> activity.on(event));
    }

    @EventHandler
    private void onActivityEvent(PlayerMoveEvent event) {
        this.doActivityAction(event.getPlayer(), activity -> activity.on(event));
    }

    @EventHandler
    private void onActivityEvent(PlayerToggleSprintEvent event) {
        this.doActivityAction(event.getPlayer(), activity -> activity.on(event));
    }

    @EventHandler
    private void onActivityEvent(PlayerToggleSneakEvent event) {
        this.doActivityAction(event.getPlayer(), activity -> activity.on(event));
    }

    @EventHandler
    private void on(PlayerArmorStandManipulateEvent event) {
        this.cancelIfCantModify(
            event, event.getPlayer(), event.getRightClicked().getLocation());
    }

    @EventHandler
    private void on(PlayerInteractAtEntityEvent event) {
        this.cancelIfCantModify(
            event, event.getPlayer(), event.getRightClicked().getLocation());
    }

    @EventHandler
    private void on(BlockPlaceEvent event) {
        this.cancelIfCantModify(event, event.getPlayer(), event.getBlock().getLocation());
    }

    @EventHandler
    private void on(BlockBreakEvent event) {
        this.cancelIfCantModify(event, event.getPlayer(), event.getBlock().getLocation());
    }

    private void cancelIfCantModify(@NonNull Cancellable event, @NonNull Player player, @NonNull Location location) {
        if (this.isPlayerCanModify(player, location)) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void on(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (this.isPlayerCanModify(event.getPlayer(), block.getLocation())) return;
        event.setUseInteractedBlock(Event.Result.DENY);
    }

    private boolean isPlayerCanModify(@NonNull Player player, @NonNull Location location) {
        UserActivity activity = this.activityManager.getActivity(player);
        if (activity == null) {
            if (this.isLobby(location.getWorld())) {
                return player.hasPermission("parkourbeat.level.edit.lobby");
            } else {
                return true;
            }
        }
        if (!(activity instanceof EditActivity) || ((EditActivity) activity).isTesting()) return false;
        return activity.getLevel().isLocationInside(location);
    }

    @EventHandler
    private void on1(PlayerMoveEvent event) {
        double yPos = event.getTo().getY();
        if (event.getFrom().getY() <= yPos) return;

        Player player = event.getPlayer();
        UserActivity activity = this.activityManager.getActivity(player);
        if (activity != null) {
            if (yPos > activity.getFallHeight()) return;
            activity.onPlayerFall();
        } else if (this.isLobby(player.getWorld())) {
            if (yPos > 0) return;
            TeleportUtils.teleportAsync(this.plugin, player, player.getWorld().getSpawnLocation());
        }
    }

    private void doActivityAction(@NonNull Player player, @NonNull Consumer<UserActivity> activityConsumer) {
        UserActivity activity = this.activityManager.getActivity(player);
        if (activity != null) activityConsumer.accept(activity);
    }

    private boolean isLobby(@NonNull World world) {
        return world == Settings.getLobbySpawn().getWorld();
    }

    private boolean isNotInLobbyOrLevel(@NonNull Player player) {
        return this.activityManager.getActivity(player) == null && !this.isLobby(player.getWorld());
    }

    @EventHandler
    private void on(AsyncChatEvent event) {
        event.renderer(ChatRenderer.viewerUnaware(this.viewerUnaware));
    }
}
