package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.levels.LevelsManager;

@RequiredArgsConstructor
public class TestCommand implements CommandExecutor {

    private final LevelsManager levelsManager;

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            @NonNull String[] args) {
        if (!sender.hasPermission("parkourbeat.test")) {
            sender.sendMessage("Недостаточно прав");
            return true;
        }
        sender.sendMessage("Nothing here");
        return true;
    }
}
