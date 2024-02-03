package ru.sortix.parkourbeat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;

public class ResourcePackListener implements Listener {

    @EventHandler
    public void onResourcePack(PlayerResourcePackStatusEvent event) {
        Game game = GameManager.getCurrentGame(event.getPlayer());
        if (game == null) {
            return;
        }
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED) {

        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {

        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {

        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            game.setCurrentState(Game.State.READY);
        }
    }

}
