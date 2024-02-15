package ru.sortix.parkourbeat.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CreateCommand implements CommandExecutor, TabCompleter {

    private final LevelEditorsManager levelEditorsManager;
    private final LevelsManager levelsManager;
    private final GameManager gameManager;

    public CreateCommand(LevelEditorsManager levelEditorsManager, LevelsManager levelsManager, GameManager gameManager) {
        this.levelEditorsManager = levelEditorsManager;
        this.levelsManager = levelsManager;
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
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

        if (levelsManager.getAllLevels().contains(args[0])) {
            sender.sendMessage("Уровень уже существует!");
        } else {
            if (!levelEditorsManager.removeEditorSession(player))
                gameManager.removeGame(player);
            Level level = levelsManager.createLevel(args[0], environment, player.getName());
            player.sendMessage("Уровень " + args[0] + " создан!");
            levelEditorsManager.createEditorSession(player, level).start();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
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

