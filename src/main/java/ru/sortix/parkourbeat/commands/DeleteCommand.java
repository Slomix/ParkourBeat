package ru.sortix.parkourbeat.commands;

import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
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

        String levelName = String.join(" ", args);
        UUID levelId = this.levelsManager.findLevelIdByName(levelName);
        if (levelId == null) {
            sender.sendMessage("Уровень \"" + levelName + "\" не найден!");
            return true;
        }

        levelsManager
                .loadLevel(levelId)
                .thenAccept(
                        level -> {
                            if (!level.getLevelSettings().getGameSettings().isOwner(sender) && !sender.isOp()) {
                                sender.sendMessage("Вы не являетесь владельцем этого уровня");
                                if (level.getWorld().getPlayers().isEmpty()) {
                                    levelsManager.unloadLevel(level.getLevelId());
                                }
                                return;
                            }
                            if (level.isEditing()) {
                                Player editorPlayer = level.getWorld().getPlayers().iterator().next();
                                levelEditorsManager.removeEditorSession(editorPlayer);
                                editorPlayer.sendMessage("Уровень \"" + levelName + "\" был удален");
                            } else {
                                for (Player player : level.getWorld().getPlayers()) {
                                    player.sendMessage("Уровень \"" + levelName + "\" был удален");
                                    gameManager.removeGame(player);
                                }
                            }
                            if (levelsManager.deleteLevel(level)) {
                                sender.sendMessage("Вы успешно удалили уровень \"" + levelName + "\"");
                            } else {
                                sender.sendMessage("Не удалось удалить уровень \"" + levelName + "\"");
                            }
                        });
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            @NonNull String[] args) {
        if (args.length == 0) return null;
        return this.levelsManager.getValidLevelNames(String.join(" ", args).toLowerCase());
    }
}
