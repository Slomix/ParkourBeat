package ru.sortix.parkourbeat.commands;

import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class CommandDelete extends ParkourBeatCommand implements TabCompleter {
    private final LevelsManager levelsManager;

    public CommandDelete(@NonNull ParkourBeat plugin) {
        super(plugin);
        this.levelsManager = plugin.get(LevelsManager.class);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!sender.hasPermission("parkourbeat.command.delete")) {
            sender.sendMessage("Недостаточно прав");
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

        this.levelsManager.loadLevel(levelId).thenAccept(level -> {
            if (level == null) {
                sender.sendMessage("Не удалось загрузить данные уровня");
                return;
            }
            if (!level.getLevelSettings().getGameSettings().isOwner(sender, true, true)) {
                sender.sendMessage("Вы не являетесь владельцем этого уровня");
                if (level.getWorld().getPlayers().isEmpty()) {
                    this.levelsManager.unloadLevelAsync(level.getLevelId());
                }
                return;
            }

            ActivityManager activityManager = this.plugin.get(ActivityManager.class);
            for (Player player : level.getWorld().getPlayers()) {
                player.sendMessage("Уровень \"" + levelName + "\" был удален");
                activityManager.setActivity(player, null);
            }

            this.levelsManager.deleteLevelAsync(level).thenAccept(success -> {
                if (success) {
                    sender.sendMessage("Вы успешно удалили уровень \"" + levelName + "\"");
                } else {
                    sender.sendMessage("Не удалось удалить уровень \"" + levelName + "\"");
                }
            });
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 0) return null;
        return this.levelsManager.getValidLevelNames(String.join(" ", args), sender);
    }
}
