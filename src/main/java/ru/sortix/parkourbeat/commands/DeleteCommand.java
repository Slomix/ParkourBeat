package ru.sortix.parkourbeat.commands;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class DeleteCommand implements CommandExecutor, TabCompleter {

    private final LevelEditorsManager levelEditorsManager;
    private final LevelsManager levelsManager;
    private final GameManager gameManager;

    public DeleteCommand(
            LevelEditorsManager levelEditorsManager,
            LevelsManager levelsManager,
            GameManager gameManager) {
        this.levelEditorsManager = levelEditorsManager;
        this.levelsManager = levelsManager;
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            @NonNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Пожалуйста, укажите уровень!");
            return true;
        }

        String worldId = args[0];
        if (levelsManager.getAllLevels().contains(worldId)) {
            Level level = levelsManager.loadLevel(worldId);
            if (!level.getLevelSettings().getGameSettings().getOwner().equals(sender.getName())
                    && !sender.isOp()) {
                sender.sendMessage("Вы не являетесь владельцем этого уровня!");
                if (level.getWorld().getPlayers().isEmpty()) {
                    levelsManager.unloadLevel(level.getName());
                }
                return true;
            }
            if (level.isEditing()) {
                Player editorPlayer = level.getWorld().getPlayers().iterator().next();
                levelEditorsManager.removeEditorSession(editorPlayer);
                editorPlayer.sendMessage("Уровень " + worldId + " был удален!");
            } else {
                for (Player player : level.getWorld().getPlayers()) {
                    player.sendMessage("Уровень " + worldId + " был удален!");
                    gameManager.removeGame(player);
                }
            }
            levelsManager.deleteLevel(worldId);
            sender.sendMessage("Вы успешно удалили " + worldId + "!");
        } else {
            sender.sendMessage("Уровень не найден!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            @NonNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return levelsManager.getAllLevels().stream()
                    .filter(level -> level.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
