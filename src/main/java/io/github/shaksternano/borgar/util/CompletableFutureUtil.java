package io.github.shaksternano.borgar.util;

import io.github.shaksternano.borgar.util.function.TriFunction;
import org.apache.commons.io.function.IOSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class CompletableFutureUtil {

    public static <T, R> CompletableFuture<R> reduceSequentiallyAsync(
        Iterable<T> iterable,
        @Nullable R identity,
        TriFunction<T, R, Integer, CompletableFuture<R>> accumulator
    ) {
        CompletableFuture<R> future = CompletableFuture.completedFuture(identity);
        var i = 0;
        for (var element : iterable) {
            var index = i;
            future = future.thenCompose(value -> {
                try {
                    return accumulator.apply(element, value, index);
                } catch (Throwable t) {
                    return CompletableFuture.failedFuture(t);
                }
            });
            i++;
        }
        return future;
    }

    public static <T, R> CompletableFuture<Optional<R>> findFirstAsync(
        Iterable<T> iterable,
        Function<T, CompletableFuture<Optional<R>>> mapper
    ) {
        return reduceSequentiallyAsync(
            iterable,
            Optional.empty(),
            (element, optional, index) -> {
                if (optional.isPresent()) {
                    return CompletableFuture.completedFuture(optional);
                } else {
                    return mapper.apply(element);
                }
            }
        );
    }

    public static <T> CompletableFuture<Void> forEachSequentiallyAsync(
        Iterable<T> iterable,
        BiFunction<T, Integer, CompletableFuture<?>> action
    ) {
        return reduceSequentiallyAsync(
            iterable,
            null,
            (element, unused, index) -> action.apply(element, index).thenApply(unused2 -> null)
        );
    }

    public static <T> Stream<T> joinAll(List<CompletableFuture<T>> futures) {
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        return futures.stream().map(CompletableFuture::join);
    }

    public static <T> CompletableFuture<T> supplyAsyncIO(IOSupplier<T> supplier, Executor executor) {
        return CompletableFuture.supplyAsync(supplier.asSupplier(), executor);
    }
}
