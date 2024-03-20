package ru.sortix.parkourbeat.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.utils.shedule.FutureUtils;

public class CommandConvertData extends ParkourBeatCommand {
    public CommandConvertData(@NonNull ParkourBeat plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!sender.hasPermission("parkourbeat.command.convertdata")) {
            sender.sendMessage("Недостаточно прав");
            return true;
        }

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            LevelsManager levelsManager = this.plugin.get(LevelsManager.class);
            if (args.length == 0) {
                AtomicInteger success = new AtomicInteger();
                AtomicInteger failed = new AtomicInteger();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (GameSettings settings : levelsManager.getAvailableLevelsSettings()) {
                    futures.add(levelsManager
                            .upgradeDataAsync(settings.getUniqueId(), null)
                            .thenAccept(success2 -> {
                                if (success2) {
                                    success.getAndIncrement();
                                } else {
                                    failed.getAndIncrement();
                                }
                            }));
                }
                FutureUtils.mergeOneByOne(futures).thenAccept(unused -> {
                    sender.sendMessage("Конвертация данных " + success + " уровней завершена успешно, а также " + failed
                            + " с ошибкой");
                });
            } else {
                GameSettings settings = levelsManager.findLevel(String.join(" ", args));
                if (settings == null) {
                    sender.sendMessage("Уровень не найден");
                    return;
                }
                levelsManager.upgradeDataAsync(settings.getUniqueId(), null).thenAccept(success -> {
                    if (success) {
                        sender.sendMessage(
                                "Конвертация данных уровня \"" + settings.getDisplayName() + "\" завершена успешно");
                    } else {
                        sender.sendMessage(
                                "Конвертация данных уровня \"" + settings.getDisplayName() + "\" завершена неудачно");
                    }
                });
            }
        });

        return true;
    }
}
