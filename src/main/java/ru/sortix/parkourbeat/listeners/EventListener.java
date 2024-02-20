package ru.sortix.parkourbeat.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;

public final class EventListener implements Listener {

    private final GameManager gameManager;
    private final LevelEditorsManager levelEditorsManager;

    public EventListener(GameManager gameManager, LevelEditorsManager levelEditorsManager) {
        this.gameManager = gameManager;
        this.levelEditorsManager = levelEditorsManager;
    }

    @EventHandler
    public void onSpawnLocation(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(Settings.getLobbySpawn());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!levelEditorsManager.removeEditorSession(player)) gameManager.removeGame(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.teleport(Settings.getLobbySpawn());
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(-40);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) event.getEntity();
        Game game = gameManager.getCurrentGame(player);
        if (game == null) {
            event.setCancelled(true);
            return;
        }
        if (game.getCurrentState() == Game.State.RUNNING) {
            if (event.getCause() == DamageCause.VOID) {
                game.stopGame(Game.StopReason.LOOSE);
                event.setCancelled(true);
            }
        } else {
            if (event.getCause() == DamageCause.VOID) {
                player.teleport(game.getLevelSettings().getWorldSettings().getSpawn());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.getEntity().spigot().respawn();
        Game game = gameManager.getCurrentGame(event.getEntity());
        if (game != null) {
            game.stopGame(Game.StopReason.LOOSE);
        } else {
            event.getEntity().teleport(Settings.getLobbySpawn());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }
}
