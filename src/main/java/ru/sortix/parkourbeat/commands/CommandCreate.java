package ru.sortix.parkourbeat.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class CommandCreate extends ParkourBeatCommand implements TabCompleter {
    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static final String CONSOLE_NAME = "CONSOLE";

    private final LevelsManager levelsManager;

    public CommandCreate(@NonNull ParkourBeat plugin) {
        super(plugin);
        this.levelsManager = plugin.get(LevelsManager.class);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {

        if (args.length < 1) {
            sender.sendMessage("Недостаточно аргументов! Используйте: /" + label + " <имя уровня> [окружение]");
            return true;
        }

        String levelName = args[0];
        World.Environment environment = World.Environment.NORMAL;

        if (args.length >= 2) {
            try {
                environment = World.Environment.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(
                        "Неверное окружение! Допустимые значения: " + Arrays.toString(World.Environment.values()));
                return true;
            }
        }

        Player player = sender instanceof Player ? (Player) sender : null;
        if (player != null) {
            this.plugin.get(ActivityManager.class).setActivity(player, null);
        }

        this.levelsManager
                .createLevel(
                        levelName,
                        environment,
                        player == null ? CONSOLE_UUID : player.getUniqueId(),
                        player == null ? CONSOLE_NAME : player.getName())
                .thenAccept(level -> {
                    if (level == null) {
                        sender.sendMessage("Не удалось создать уровень \"" + levelName + "\"");
                        return;
                    }
                    if (player == null) {
                        sender.sendMessage("Уровень \"" + levelName + "\" создан");
                        return;
                    }
                    EditActivity.createAsync(this.plugin, player, level).thenAccept(editActivity -> {
                        if (editActivity == null) {
                            sender.sendMessage("Уровень \"" + levelName
                                    + "\" создан, однако не удалось запустить редактор уровня");
                            return;
                        }
                        this.plugin.get(ActivityManager.class).setActivity(player, editActivity);
                        sender.sendMessage("Уровень \"" + levelName + "\" создан");
                    });
                });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
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
