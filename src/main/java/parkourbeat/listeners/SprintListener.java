package parkourbeat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import parkourbeat.game.Game;
import parkourbeat.game.GameManager;

public class SprintListener implements Listener {

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent event) {
        Game game = GameManager.getCurrentGame(event.getPlayer());
        if (game == null) {
            return;
        }
        if (game.getCurrentState() == Game.State.RUNNING) {
            game.getGameMoveHandler().onRunningState(event);
        }
    }

}
