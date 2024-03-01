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
import ru.sortix.parkourbeat.levels.LevelsManager;

public class EditCommand implements CommandExecutor, TabCompleter {

    private final LevelEditorsManager levelEditorsManager;
    private final LevelsManager levelsManager;
    private final GameManager gameManager;

    public EditCommand(
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Недостаточно аргументов! Используйте: /edit <имя уровня>");
            return false;
        }

        if (!levelsManager.getAllLevels().contains(args[0])) {
            player.sendMessage("Уровень не найден!");
            return true;
        }
        levelsManager
                .loadLevel(args[0])
                .thenAccept(
                        level -> {
                            if (!level.getLevelSettings().getGameSettings().getOwner().equals(player.getName())) {
                                player.sendMessage("Вы не являетесь владельцем этого уровня!");
                                if (level.getWorld().getPlayers().isEmpty()) {
                                    levelsManager.unloadLevel(level.getName());
                                }
                                return;
                            }
                            if (level.isEditing()) {
                                player.sendMessage("Вы и так уже редактируете данный уровень!");
                                return;
                            }
                            if (!levelEditorsManager.removeEditorSession(player)) gameManager.removeGame(player);
                            levelEditorsManager.createEditorSession(player, level).start();
                        });
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
