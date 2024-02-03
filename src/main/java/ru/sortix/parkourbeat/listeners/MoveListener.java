package ru.sortix.parkourbeat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.game.GameMoveHandler;

public class MoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Game game = GameManager.getCurrentGame(player);
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

}
