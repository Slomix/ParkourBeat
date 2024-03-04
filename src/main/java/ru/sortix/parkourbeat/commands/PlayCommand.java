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
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class PlayCommand implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;
    private final LevelsManager levelsManager;
    private final LevelEditorsManager levelEditorsManager;

    public PlayCommand(
            GameManager gameManager,
            LevelsManager levelsManager,
            LevelEditorsManager levelEditorsManager) {
        this.gameManager = gameManager;
        this.levelsManager = levelsManager;
        this.levelEditorsManager = levelEditorsManager;
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        if (this.levelEditorsManager.getEditorSession(player) != null) {
            player.sendMessage("Для начала игры выйдите из режима редактора");
            return true;
        }

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

        Level level = this.levelsManager.getLoadedLevel(levelId);
        if (level != null) {
            if (level.isEditing()) {
                player.sendMessage("Данный уровень недоступен для игры, т.к. он сейчас редактируется");
                return true;
            }
        }

        Game game = this.gameManager.getCurrentGame(player);
        if (game != null) {
            if (game.getLevel().equals(level)) {
                player.sendMessage("Вы уже на этом уровне!");
                return true;
            }
            this.gameManager.removeGame(player);
        }

        if (level == null) {
            player.sendMessage("Загрузка уровня...");
        }

        gameManager
                .createNewGame(player, levelId)
                .thenAccept(
                        success -> {
                            if (level == null) {
                                player.sendMessage("Уровень загружен");
                            }
                            if (!success) {
                                player.sendMessage("Не удалось начать игру");
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
