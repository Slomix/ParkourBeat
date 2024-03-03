package ru.sortix.parkourbeat.game.movement;

import lombok.NonNull;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class GameMoveHandler {

    private final Game game;
    private final Location startBorder, finishBorder;
    private final MovementAccuracyChecker accuracyChecker;
    private BukkitTask task;

    public GameMoveHandler(Game game) {
        this.game = game;

        WorldSettings worldSettings = game.getLevelSettings().getWorldSettings();
        this.accuracyChecker =
                new MovementAccuracyChecker(
                        worldSettings.getWaypoints(), game.getLevelSettings().getDirectionChecker());

        startBorder = worldSettings.getStartBorder().toLocation(worldSettings.getWorld());
        finishBorder = worldSettings.getFinishBorder().toLocation(worldSettings.getWorld());
    }

    public MovementAccuracyChecker getAccuracyChecker() {
        return accuracyChecker;
    }

    public void onPreparingState(PlayerMoveEvent event) {}

    public void onReadyState(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        LevelSettings settings = game.getLevelSettings();
        if (settings.getDirectionChecker().isCorrectDirection(startBorder, player.getLocation())) {
            game.start();
            if ((task == null || task.isCancelled()) && !player.isSprinting()) {
                startDamageTask(player, 6, "§cНе переставайте бежать", Game.StopReason.STOP_MOVEMENT);
            }
        }
    }

    public void onRunningState(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        LevelSettings settings = game.getLevelSettings();
        if (settings.getDirectionChecker().isCorrectDirection(finishBorder, player.getLocation())) {
            game.stopGame(Game.StopReason.FINISH);
            return;
        }
        if (!isLookingAtFinish(player)) {
            game.stopGame(Game.StopReason.WRONG_DIRECTION);
            return;
        }
        accuracyChecker.onPlayerLocationChange(event.getTo());
        player
                .spigot()
                .sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(
                                "§aТочность: " + accuracyChecker.getAccuracy() * 100 + "%"));
    }

    public void onRunningState(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (!event.isSprinting()) {
            startDamageTask(player, 6, "§cНе переставайте бежать", Game.StopReason.STOP_MOVEMENT);
        } else {
            if (task != null) {
                task.cancel();
            }
        }
    }

    private boolean isLookingAtFinish(Player player) {
        Vector playerDir = player.getLocation().getDirection();
        Vector targetDir = finishBorder.toVector().subtract(startBorder.toVector());
        double angle = playerDir.angle(targetDir);
        return Math.toDegrees(angle) < 100;
    }

    private void startDamageTask(
            Player player, int damage, String reason, Game.StopReason stopReason) {
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HURT, 1, 1);

        this.task =
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline() || game.getCurrentState() != Game.State.RUNNING) {
                            this.cancel();
                            return;
                        }

                        player.sendTitle(reason, null, 0, 5, 5);
                        boolean stopped;
                        if (player.getHealth() <= damage) {
                            game.stopGame(stopReason);
                            stopped = true;
                        } else {
                            player.damage(damage);
                            stopped = false;
                        }

                        if (stopped) {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(this.getPlugin(), 0, 2);
    }

    @NonNull public Plugin getPlugin() {
        return this.game.getPlugin();
    }
}
