package parkourbeat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import parkourbeat.game.Game;
import parkourbeat.game.GameManager;

public class MoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Game game = GameManager.getCurrentGame(player);
        if (game == null) {
            return;
        }
        Game.State state = game.getCurrentState();
        if (state == Game.State.PREPARING) {
            game.getGameMoveHandler().onPreparingState(event);
        } else if (state == Game.State.READY) {
            game.getGameMoveHandler().onReadyState(event);
        } else if (state == Game.State.RUNNING) {
            game.getGameMoveHandler().onRunningState(event);
        }
    }

}
