package ru.sortix.parkourbeat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class PlayCommand implements CommandExecutor {

    private final GameManager gameManager;
    private final LevelsManager levelsManager;

    public PlayCommand(GameManager gameManager, LevelsManager levelsManager) {
        this.gameManager = gameManager;
        this.levelsManager = levelsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                sender.sendMessage("Please specify a level!");
                return true;
            }

            String worldId = args[0];
            Game game = gameManager.getCurrentGame(player);

            if (game != null) {
                if (game.getLevelSettings().getWorldSettings().getWorld().getName().equals(worldId)) {
                    player.sendMessage("You are already in this level!");
                    return true;
                } else {
                    gameManager.removeGame(player);
                }
            }

            if (levelsManager.getAllLevels().contains(worldId)) {
                gameManager.createNewGame(player, worldId);
            } else {
                player.sendMessage("Level not found!");
            }
        } else {
            sender.sendMessage("Command only for players!");
        }
        return true;
    }

}
