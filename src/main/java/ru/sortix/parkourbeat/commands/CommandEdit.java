package ru.sortix.parkourbeat.commands;

import java.util.List;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.inventory.type.LevelsListMenu;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class CommandEdit extends ParkourBeatCommand implements TabCompleter {
    private final LevelsManager levelsManager;

    public CommandEdit(@NonNull ParkourBeat plugin) {
        super(plugin);
        this.levelsManager = plugin.get(LevelsManager.class);
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

        if (args.length == 0) {
            new LevelsListMenu(this.plugin, player, player.getUniqueId()).open(player);
            return true;
        }

        String levelName = String.join(" ", args);
        GameSettings settings = this.levelsManager.findLevelSettingsByUniqueName(levelName);

        if (settings == null) {
            sender.sendMessage("Уровень \"" + levelName + "\" не найден!");
            return true;
        }

        LevelsListMenu.startEditing(this.plugin, player, settings);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player)) return null;
        if (args.length == 0) return null;
        return this.levelsManager.getUniqueLevelNames(String.join(" ", args), sender);
    }
}
