package ru.sortix.parkourbeat.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class CreateCommand implements CommandExecutor, TabCompleter {

    private final LevelEditorsManager levelEditorsManager;
    private final LevelsManager levelsManager;
    private final GameManager gameManager;

    public CreateCommand(
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
        Player player = (Player) sender;
        if (args.length < 2) {
            sender.sendMessage("Недостаточно аргументов! Используйте: /create <имя уровня> <окружение>");
            return true;
        }
        World.Environment environment;
        try {
            environment = World.Environment.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Неверное окружение! Допустимые значения: NORMAL, NETHER, THE_END");
            return true;
        }

        String levelName = args[0];
        if (!levelEditorsManager.removeEditorSession(player)) {
            gameManager.removeGame(player);
        }
        levelsManager
                .createLevel(levelName, environment, player.getUniqueId(), player.getName())
                .thenAccept(
                        level -> {
                            if (level == null) {
                                player.sendMessage("Не удалось создать уровень \"" + levelName + "\"");
                                return;
                            }
                            player.sendMessage("Уровень \"" + levelName + "\" создан");
                            levelEditorsManager.createEditorSession(player, level).start();
                        });
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            String environment = args[1].toLowerCase();
            return Arrays.stream(World.Environment.values())
                    .map(Enum::name)
                    .filter(e -> e.toLowerCase().startsWith(environment))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
