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
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.PlayActivity;
import ru.sortix.parkourbeat.inventory.levels.LevelsListMenu;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class CommandPlay extends ParkourBeatCommand implements TabCompleter {
    private final LevelsManager levelsManager;

    public CommandPlay(@NonNull ParkourBeat plugin) {
        super(plugin);
        this.levelsManager = plugin.get(LevelsManager.class);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new LevelsListMenu(this.plugin, player).open(player);
            return true;
        }

        String levelName = String.join(" ", args);
        UUID levelId = this.levelsManager.findLevelIdByName(levelName);
        if (levelId == null) {
            sender.sendMessage("Уровень \"" + levelName + "\" не найден!");
            return true;
        }
        startPlaying(this.plugin, player, levelId);
        return true;
    }

    public static void startPlaying(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull UUID levelId) {
        Level level = plugin.get(LevelsManager.class).getLoadedLevel(levelId);
        if (level != null) {
            if (level.isEditing()) {
                player.sendMessage("Данный уровень недоступен для игры, т.к. он сейчас редактируется");
                return;
            }
        }

        UserActivity activity = plugin.get(ActivityManager.class).getActivity(player);
        if (activity instanceof PlayActivity && activity.getLevel() == level) {
            player.sendMessage("Вы уже на этом уровне!");
            return;
        }

        boolean levelLoaded = level != null;
        if (!levelLoaded) player.sendMessage("Загрузка уровня...");

        PlayActivity.createAsync(plugin, player, levelId, false).thenAccept(playActivity -> {
            if (playActivity == null) {
                player.sendMessage("Не удалось запустить игру");
                return;
            }
            plugin.get(ActivityManager.class).setActivity(player, playActivity);
            if (!levelLoaded) player.sendMessage("Уровень загружен");
        });
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 0) return null;
        return this.levelsManager.getValidLevelNames(String.join(" ", args), null);
    }
}
