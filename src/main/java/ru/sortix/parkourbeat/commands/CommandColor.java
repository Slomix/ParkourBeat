package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.ParkourBeat;

public class CommandColor extends ParkourBeatCommand {
    public CommandColor(@NonNull ParkourBeat plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        sender.sendMessage("Используйте предмет \"Параметры уровня\"");
        return true;
    }
}
