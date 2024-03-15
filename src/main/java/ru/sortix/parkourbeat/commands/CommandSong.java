package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.EditActivity;

public class CommandSong extends ParkourBeatCommand {
    public CommandSong(@NonNull ParkourBeat plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда только для игроков!");
            return true;
        }
        Player player = (Player) sender;
        UserActivity activity = this.plugin.get(ActivityManager.class).getActivity(player);
        if (!(activity instanceof EditActivity)) {
            player.sendMessage("Вы не в режиме редактирования!");
            return true;
        }
        ((EditActivity) activity).openSongMenu();
        return true;
    }
}
