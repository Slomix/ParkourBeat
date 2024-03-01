package ru.sortix.parkourbeat.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.levels.Level;
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
        Logger logger = this.levelsManager.getPlugin().getLogger();
        long startAtMills = System.currentTimeMillis();
        int amount = Integer.parseInt(args[0]);
        List<CompletableFuture<Level>> futures = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            futures.add(
                    this.levelsManager.createLevel(
                            UUID.randomUUID().toString(), World.Environment.NORMAL, "CONSOLE"));
        }
        CompletableFuture.allOf()
                .thenAccept(
                        unused -> {
                            logger.info(
                                    "Created "
                                            + amount
                                            + " world(s) in "
                                            + (System.currentTimeMillis() - startAtMills)
                                            + " ms");
                        });
        logger.info(
                "Command complete with "
                        + amount
                        + " world(s) in "
                        + (System.currentTimeMillis() - startAtMills)
                        + " ms");
        return true;
    }
}
