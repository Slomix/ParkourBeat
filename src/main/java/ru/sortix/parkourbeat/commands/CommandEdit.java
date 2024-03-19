package ru.sortix.parkourbeat.commands;

import java.util.Collection;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.inventory.type.LevelsListMenu;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class CommandEdit extends ParkourBeatCommand {
    public CommandEdit(@NonNull ParkourBeat plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!sender.hasPermission("parkourbeat.command.edit")) {
            sender.sendMessage("Недостаточно прав");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков");
            return true;
        }

        Player player = (Player) sender;
        boolean bypassForAdmins = args.length >= 1 && args[0].equals("*");
        new LevelsListMenu(
                        this.plugin, player.getUniqueId(), bypassForAdmins, player.hasPermission("parkourbeat.admin"))
                .open(player);
        return true;
    }

    public static void startEditing(
            @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull GameSettings settings) {
        plugin.get(LevelsManager.class).loadLevel(settings.getLevelId()).thenAccept(level -> {
            if (level == null) {
                player.sendMessage("Не удалось загрузить данные уровня");
                return;
            }

            if (level.isEditing()) {
                player.sendMessage("Данный уровень уже редактируется");
                return;
            }

            ActivityManager activityManager = plugin.get(ActivityManager.class);

            Collection<Player> playersOnLevel = activityManager.getPlayersOnTheLevel(level);
            playersOnLevel.removeIf(player1 -> settings.isOwner(player1, true, true));

            if (!playersOnLevel.isEmpty()) {
                player.sendMessage("Нельзя редактировать уровень, на котором находятся игроки");
                return;
            }

            EditActivity.createAsync(plugin, player, level).thenAccept(editActivity -> {
                if (editActivity == null) {
                    player.sendMessage("Не удалось запустить редактор уровня");
                    return;
                }
                activityManager.setActivity(player, editActivity);
            });
        });
    }
}
