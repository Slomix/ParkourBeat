package ru.sortix.parkourbeat.utils.shedule;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class FutureUtils {
    private static final Runnable EMPTY_RUNNABLE = () -> {
    };

    @NonNull
    public CompletableFuture<Void> mergeParallel(@NonNull Collection<CompletableFuture<?>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @NonNull
    public <T> CompletableFuture<Void> mergeOneByOne(@NonNull Collection<CompletableFuture<T>> futures) {
        CompletableFuture<Void> result = CompletableFuture.completedFuture(null);
        for (CompletableFuture<T> future : futures) {
            result = result.thenCompose(unused -> future.thenRun(EMPTY_RUNNABLE));
        }
        return result;
    }
}
