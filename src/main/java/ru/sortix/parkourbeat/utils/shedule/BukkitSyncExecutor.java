package ru.sortix.parkourbeat.utils.shedule;

import java.util.concurrent.Executor;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class BukkitSyncExecutor implements Executor {
    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public BukkitSyncExecutor(@NonNull Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.scheduler.runTask(this.plugin, command);
    }
}
