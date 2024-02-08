package ru.sortix.parkourbeat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class EventListener implements Listener {

    private final GameManager gameManager;
    private final LevelEditorsManager levelEditorsManager;

    public EventListener(GameManager gameManager, LevelEditorsManager levelEditorsManager) {
        this.gameManager = gameManager;
        this.levelEditorsManager = levelEditorsManager;
    }

    @EventHandler
    public void onSpawnLocation(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(Settings.getExitLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        gameManager.removeGame(event.getPlayer());
        levelEditorsManager.removeEditorSession(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.teleport(Settings.getExitLocation());
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(-40);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) event.getEntity();
        Game game = gameManager.getCurrentGame(player);
        if (game == null) {
            return;
        }
        if (game.getCurrentState() != Game.State.RUNNING) {
            if (event.getCause() == DamageCause.VOID) {
                game.stopGame(Game.StopReason.LOOSE);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(ParkourBeat.getPlugin(), () -> {
            event.getEntity().spigot().respawn();
            Game game = gameManager.getCurrentGame(event.getEntity());
            if (game != null) {
                game.stopGame(Game.StopReason.LOOSE);
            } else {
                event.getEntity().teleport(Settings.getExitLocation());
            }
        });
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

}
