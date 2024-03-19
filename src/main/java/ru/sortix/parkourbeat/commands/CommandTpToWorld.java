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
import ru.sortix.parkourbeat.activity.type.SpectateActivity;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class CommandTpToWorld extends ParkourBeatCommand implements TabCompleter {
    private final LevelsManager levelsManager;

    public CommandTpToWorld(@NonNull ParkourBeat plugin) {
        super(plugin);
        this.levelsManager = plugin.get(LevelsManager.class);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!sender.hasPermission("parkourbeat.command.tptoworld")) {
            sender.sendMessage("Недостаточно прав");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            sender.sendMessage("Укажите название уровня. Для телепортации на спаун используйте /spawn");
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
            if (level.getWorld() == player.getWorld()) {
                sender.sendMessage("Вы уже в этом мире");
                return;
            }

            SpectateActivity.createAsync(this.plugin, player, level).thenAccept(spectateActivity -> {
                TeleportUtils.teleportAsync(this.plugin, player, level.getSpawn())
                        .thenAccept(success -> {
                            if (!success) return;
                            this.plugin.get(ActivityManager.class).setActivity(player, spectateActivity);
                        });
            });
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player)) return null;
        if (args.length == 0) return null;
        return this.levelsManager.getValidLevelNames(String.join(" ", args), null);
    }
}
