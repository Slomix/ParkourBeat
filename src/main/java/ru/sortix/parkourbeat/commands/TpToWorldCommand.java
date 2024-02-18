package ru.sortix.parkourbeat.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class TpToWorldCommand implements CommandExecutor, TabCompleter {

	private final LevelsManager levelsManager;
	private final GameManager gameManager;
	private final LevelEditorsManager levelEditorsManager;

	public TpToWorldCommand(
			LevelEditorsManager levelEditorsManager,
			GameManager gameManager,
			LevelsManager levelsManager) {
		this.levelsManager = levelsManager;
		this.gameManager = gameManager;
		this.levelEditorsManager = levelEditorsManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length > 0) {
				String worldId = args[0];

				if (!levelsManager.getLoadedLevels().contains(worldId)) {
					sender.sendMessage("World not loaded!");
					return true;
				}
				Level level = levelsManager.getLevelWorld(worldId);
				if (level.getWorld().equals(player.getWorld())) {
					sender.sendMessage("You are already in this world!");
					return true;
				}
				player.teleport(level.getLevelSettings().getWorldSettings().getSpawn());
				player.setGameMode(GameMode.SPECTATOR);
				player.sendMessage("Teleported to parkourbeat level " + level.getName());
			} else {
				player.teleport(Settings.getLobbySpawn());
				player.sendMessage("Teleported to lobby");
				player.setGameMode(GameMode.ADVENTURE);
			}
			if (!levelEditorsManager.removeEditorSession(player)) gameManager.removeGame(player);
		} else {
			sender.sendMessage("Command only for players!");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(
			CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> tabComplete = new ArrayList<>();
		if (sender instanceof Player && args.length == 1) {
			for (String inGameWorld : levelsManager.getLoadedLevels()) {
				if (inGameWorld.toLowerCase().startsWith(args[0].toLowerCase())) {
					tabComplete.add(inGameWorld);
				}
			}
		}
		return tabComplete;
	}
}
