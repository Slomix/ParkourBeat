package ru.sortix.parkourbeat.utils.shedule;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class CurrentThreadExecutor implements Executor {
    @Override
    public void execute(@NotNull Runnable command) {
        command.run();
    }
}
