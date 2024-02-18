package ru.sortix.parkourbeat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;

public class SprintListener implements Listener {

	private final GameManager gameManager;

	public SprintListener(GameManager gameManager) {
		this.gameManager = gameManager;
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
}
