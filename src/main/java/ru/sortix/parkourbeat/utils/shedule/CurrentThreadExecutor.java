package ru.sortix.parkourbeat.utils.shedule;

import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;

public class CurrentThreadExecutor implements Executor {
    @Override
    public void execute(@NotNull Runnable command) {
        command.run();
    }
}
