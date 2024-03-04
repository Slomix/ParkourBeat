package ru.sortix.parkourbeat.listeners;

import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.EditorSession;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.game.movement.GameMoveHandler;

public final class GamesListener implements Listener {
    private final GameManager gameManager;
    private final LevelEditorsManager levelEditorsManager;

    public GamesListener(
            @NonNull ParkourBeat plugin,
            @NonNull GameManager gameManager,
            @NonNull LevelEditorsManager levelEditorsManager) {
        this.gameManager = gameManager;
        this.levelEditorsManager = levelEditorsManager;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (this.getWorldType(player) != WorldType.PB_LEVEL) continue;
            player.teleport(Settings.getLobbySpawn());
        }
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
        if (this.getWorldType(player) == WorldType.NON_PB) return;
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
    private void on(PlayerMoveEvent event) {
        if (event.getFrom().getY() <= event.getTo().getY()) return;
        Game game = this.gameManager.getCurrentGame(event.getPlayer());
        if (game == null) return;
        if (event.getTo().getY() > game.getLevelSettings().getWorldSettings().getMinWorldHeight())
            return;
        game.stopGame(Game.StopReason.FALL);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (this.getWorldType(player) == WorldType.NON_PB) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void on(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = ((Player) event.getEntity());
        if (this.getWorldType(player) == WorldType.NON_PB) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (this.getWorldType(player) == WorldType.NON_PB) return;
        event.setKeepInventory(true);
        event.getDrops().clear();
        player.spigot().respawn();
        Game game = this.gameManager.getCurrentGame(player);
        if (game != null) {
            game.stopGame(Game.StopReason.DEATH);
            return;
        }
        player.teleport(Settings.getLobbySpawn());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (this.getWorldType((Player) event.getEntity()) == WorldType.NON_PB) return;
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Game game = gameManager.getCurrentGame(player);
        if (game == null) {
            return;
        }

        Game.State state = game.getCurrentState();
        GameMoveHandler gameMoveHandler = game.getGameMoveHandler();
        if (state == Game.State.PREPARING) {
            gameMoveHandler.onPreparingState(event);
        } else if (state == Game.State.READY) {
            gameMoveHandler.onReadyState(event);
        } else if (state == Game.State.RUNNING) {
            gameMoveHandler.onRunningState(event);
        }
    }

    @EventHandler
    public void onResourcePack(PlayerResourcePackStatusEvent event) {
        Game game = gameManager.getCurrentGame(event.getPlayer());
        if (game == null) return;
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED) {

        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {

        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {

        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            game.setCurrentState(Game.State.READY);
        }
    }

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent event) {
        Game game = gameManager.getCurrentGame(event.getPlayer());
        if (game == null) {
            return;
        }
        if (game.getCurrentState() == Game.State.RUNNING) {
            game.getGameMoveHandler().onRunningState(event);
        }
    }

    @EventHandler
    private void on(PlayerInteractEvent event) {
        if (this.getWorldType(event.getPlayer()) == WorldType.NON_PB) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        EditorSession editorSession = this.levelEditorsManager.getEditorSession(event.getPlayer());
        if (editorSession != null && block.getWorld() == editorSession.getLevel().getWorld()) return;
        if (event.getPlayer().hasPermission("parkourbeat.level.edit.anytime")) return;
        event.setUseInteractedBlock(Event.Result.DENY);
    }

    @NonNull private WorldType getWorldType(@NonNull Player player) {
        // TODO Стандартизировать названия миров и проверять по ним, а не при помощи gameManager:
        //  https://github.com/Slomix/ParkourBeat/issues/9
        Game game = this.gameManager.getCurrentGame(player);
        if (game != null) {
            return WorldType.PB_LEVEL;
        }
        if (this.levelEditorsManager.getEditorSession(player) != null) {
            return WorldType.PB_LEVEL;
        }
        if (Settings.getLobbySpawn().getWorld() == player.getWorld()) {
            return WorldType.PB_LOBBY;
        }
        return WorldType.NON_PB;
    }

    public enum WorldType {
        PB_LOBBY,
        PB_LEVEL,
        NON_PB
    }
}
