package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.constant.Messages;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "delete")
@RequiredArgsConstructor
public class CommandDelete {

    private final ParkourBeat plugin;

    public static void deleteLevel(
        @NonNull ParkourBeat plugin, @NonNull CommandSender sender, @NonNull GameSettings settings) {
        LevelsManager levelsManager = plugin.get(LevelsManager.class);
        ActivityManager activityManager = plugin.get(ActivityManager.class);

        Level loadedLevel = levelsManager.getLoadedLevel(settings.getUniqueId());
        if (loadedLevel != null) {
            for (Player player : loadedLevel.getWorld().getPlayers()) {
                if (player != sender) {
                    player.sendMessage(
                        String.format(Messages.LEVEL_DELETION_ALREADY_DELETED, settings.getDisplayName()));
                }
                activityManager.switchActivity(player, null, Settings.getLobbySpawn());
            }
        }

        levelsManager.deleteLevelAsync(settings).thenAccept(successResult -> {
            if (Boolean.TRUE.equals(successResult)) {
                sender.sendMessage(String.format(Messages.SUCCESSFUL_LEVEL_DELETION, settings.getDisplayName()));
            } else {
                sender.sendMessage(String.format(Messages.FAILED_LEVEL_DELETION, settings.getDisplayName()));
            }
        });
    }

    @Execute
    @Permission(COMMAND_PERMISSION + ".delete")
    public String onCommand(@Context CommandSender sender, @Arg("settings-console-owning") GameSettings gameSettings) {
        if (sender instanceof Player) {
            return Messages.USE_LEVEL_PARAMETERS_ITEM;
        }

        if (!gameSettings.isOwner(sender, true, true)) {
            return Messages.NOT_LEVEL_OWNER;
        }

        deleteLevel(this.plugin, sender, gameSettings);
        return null;
    }
}
