package io.atomix.utils.concurrent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Futures {

    /**
     * Returns a new CompletableFuture completed with a list of computed values
     * when all of the given CompletableFuture complete.
     *
     * @param futures the CompletableFutures
     * @param <T>     value type of CompletableFuture
     * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
     */
    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<Stream<T>> allOf(Stream<CompletableFuture<T>> futures) {
        CompletableFuture<T>[] futuresArray = futures.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futuresArray)
                .thenApply(v -> Stream.of(futuresArray).map(CompletableFuture::join));
    }

    /**
     * Returns a new CompletableFuture completed with a list of computed values
     * when all of the given CompletableFuture complete.
     *
     * @param futures the CompletableFutures
     * @param <T>     value type of CompletableFuture
     * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
     */
    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    /**
     * Returns a new CompletableFuture completed by reducing a list of computed values
     * when all of the given CompletableFuture complete.
     *
     * @param futures    the CompletableFutures
     * @param reducer    reducer for computing the result
     * @param emptyValue zero value to be returned if the input future list is empty
     * @param <T>        value type of CompletableFuture
     * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
     */
    public static <T> CompletableFuture<T> allOf(
            List<CompletableFuture<T>> futures, BinaryOperator<T> reducer, T emptyValue) {
        return allOf(futures).thenApply(resultList -> resultList.stream().reduce(reducer).orElse(emptyValue));
    }
}
