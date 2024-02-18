package ru.sortix.parkourbeat.commands;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class PlayCommand implements CommandExecutor, TabCompleter {

	private final GameManager gameManager;
	private final LevelsManager levelsManager;

	public PlayCommand(GameManager gameManager, LevelsManager levelsManager) {
		this.gameManager = gameManager;
		this.levelsManager = levelsManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Команда только для игроков!");
			return true;
		}

		Player player = (Player) sender;

		Level currentLevel = levelsManager.getLevelWorld(player.getWorld().getName());
		if (currentLevel != null && currentLevel.isEditing()) {
			player.sendMessage("Вы не можете играть в другие уровни находясь в редакторе!");
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage("Пожалуйста, укажите уровень!");
			return true;
		}

		String worldId = args[0];
		Game game = gameManager.getCurrentGame(player);

		if (game != null) {
			if (game.getLevelSettings().getWorldSettings().getWorld().getName().equals(worldId)) {
				player.sendMessage("Вы уже на этом уровне!");
				return true;
			} else {
				gameManager.removeGame(player);
			}
		}

		if (levelsManager.getAllLevels().contains(worldId)) {
			gameManager.createNewGame(player, worldId);
		} else {
			player.sendMessage("Уровень не найден!");
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(
			CommandSender sender, Command command, String s, String[] args) {
		if (args.length == 1) {
			String input = args[0].toLowerCase();
			return levelsManager.getAllLevels().stream()
					.filter(level -> level.toLowerCase().startsWith(input))
					.collect(Collectors.toList());
		}
		return null;
	}
}
