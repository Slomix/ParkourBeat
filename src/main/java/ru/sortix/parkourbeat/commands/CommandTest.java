package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.ParkourBeat;

public class CommandTest extends ParkourBeatCommand {
    public CommandTest(@NonNull ParkourBeat plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!sender.hasPermission("parkourbeat.command.test")) {
            sender.sendMessage("Недостаточно прав");
            return true;
        }

        sender.sendMessage("Nothing here");
        return true;
    }
}
