package ru.sortix.parkourbeat.utils.shedule;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class FutureUtils {
    private static final Runnable EMPTY_RUNNABLE = () -> {
    };

    @NonNull
    public CompletableFuture<Void> mergeParallel(@NonNull CompletableFuture<?>... futures) {
        return CompletableFuture.allOf(futures);
    }

    @NonNull
    public CompletableFuture<Void> mergeParallel(@NonNull Collection<CompletableFuture<?>> futures) {
        return mergeParallel(futures.toArray(new CompletableFuture[0]));
    }

    @NonNull
    @SafeVarargs
    public <T> CompletableFuture<Void> mergeOneByOne(@NonNull CompletableFuture<T>... futures) {
        return mergeOneByOne(Arrays.asList(futures));
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
