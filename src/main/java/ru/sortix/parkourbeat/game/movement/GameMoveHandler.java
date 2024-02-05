package ru.sortix.parkourbeat.game.movement;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.location.Region;

public class GameMoveHandler {

    private final Game game;
    private final Location startLoc, finishLoc;
    private BukkitTask task;
    private final MovementAccuracyChecker accuracyChecker;

    public GameMoveHandler(Game game) {
        this.game = game;

        WorldSettings worldSettings = game.getLevelSettings().getWorldSettings();
        this.accuracyChecker = new MovementAccuracyChecker(
                worldSettings.getParticlePoints(),
                game.getLevelSettings().getDirectionChecker()
        );

        Region startRegion = worldSettings.getStartRegion();
        Region finishRegion = worldSettings.getFinishRegion();

        startLoc = startRegion.getCenter().toLocation(worldSettings.getWorld());
        finishLoc = finishRegion.getCenter().toLocation(worldSettings.getWorld());
    }

    public MovementAccuracyChecker getAccuracyChecker() {
        return accuracyChecker;
    }

    public void onPreparingState(PlayerMoveEvent event) {

    }

    public void onReadyState(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        LevelSettings settings = game.getLevelSettings();
        WorldSettings worldSettings = settings.getWorldSettings();
        if (worldSettings.getStartRegion().isInside(player.getLocation())) {
            game.start();
        }
    }

    public void onRunningState(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        LevelSettings settings = game.getLevelSettings();
        WorldSettings worldSettings = settings.getWorldSettings();
        if (worldSettings.getFinishRegion().isInside(player.getLocation())) {
            game.stopGame(Game.StopReason.FINISH);
            return;
        }
        if (!isLookingAtFinish(player)) {
            game.stopGame(Game.StopReason.WRONG_DIRECTION);
            return;
        }
        accuracyChecker.onPlayerLocationChange(event.getTo());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aТочность: " + accuracyChecker.getAccuracy() * 100 + "%"));
    }

    public void onRunningState(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (!event.isSprinting()) {
            startDamageTask(player, 6, "§cНе переставайте бежать");
        } else {
            if (task != null) {
                task.cancel();
            }
        }
    }

    private boolean isLookingAtFinish(Player player) {
        Vector playerDir = player.getLocation().getDirection();
        Vector targetDir = finishLoc.toVector().subtract(startLoc.toVector());
        double angle = playerDir.angle(targetDir);
        return Math.toDegrees(angle) < 100;
    }

    private void startDamageTask(Player player, int damage, String reason) {
        task = Bukkit.getScheduler().runTaskTimer(ParkourBeat.getPlugin(), () -> {
            if (!player.isOnline() || game.getCurrentState() != Game.State.RUNNING) {
                task.cancel();
                return;
            }
            damagePlayer(player, damage, reason);
            if (player.isDead() || player.getHealth() <= 0) {
                task.cancel();
            }
        }, 0, 2);
    }

    private void damagePlayer(Player player, int damage, String reason) {
        player.damage(damage);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(reason));
    }
}
