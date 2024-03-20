package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.constant.Messages;
import ru.sortix.parkourbeat.levels.LevelsManager;

import java.util.Optional;
import java.util.UUID;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "create")
public class CommandCreate {
    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static final String CONSOLE_NAME = "CONSOLE";

    private final ParkourBeat plugin;
    private final LevelsManager levelsManager;

    public CommandCreate(@NonNull ParkourBeat plugin) {
        this.plugin = plugin;
        this.levelsManager = plugin.get(LevelsManager.class);
    }

    @Execute
    @Permission(COMMAND_PERMISSION + ".create")
    public void onCommand(
            @Context CommandSender sender,
            @Arg("levelName") String levelName,
            @Arg("environment") Optional<World.Environment> environmentOpt) {
        World.Environment environment = environmentOpt.orElse(World.Environment.NORMAL);

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
                        sender.sendMessage(String.format(Messages.FAILED_LEVEL_CREATION, levelName));
                        return;
                    }
                    if (player == null) {
                        sender.sendMessage(String.format(Messages.SUCCESSFUL_LEVEL_CREATION, levelName));
                        return;
                    }
                    EditActivity.createAsync(this.plugin, player, level).thenAccept(editActivity -> {
                        if (editActivity == null) {
                            sender.sendMessage(String.format(
                                    Messages.SUCCESSFUL_LEVEL_CREATION_WITH_EDITOR_LAUNCH_PROBLEM, levelName));
                            return;
                        }
                        this.plugin.get(ActivityManager.class).setActivity(player, editActivity);
                        sender.sendMessage(String.format(Messages.SUCCESSFUL_LEVEL_CREATION, levelName));
                    });
                });
    }
}
