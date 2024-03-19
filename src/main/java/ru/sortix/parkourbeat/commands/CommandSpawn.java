package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class CommandSpawn extends ParkourBeatCommand {
    public CommandSpawn(@NonNull ParkourBeat plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!sender.hasPermission("parkourbeat.command.spawn")) {
            sender.sendMessage("Недостаточно прав");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков");
            return true;
        }

        Player player = (Player) sender;
        this.plugin.get(ActivityManager.class).setActivity(player, null);
        TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn());
        return true;
    }
}
