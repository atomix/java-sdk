// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.concurrent;

import io.atomix.client.PrimitiveException;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Retry utilities.
 */
public final class Retries {
    private static final Duration BASE_DELAY = Duration.ofMillis(10);

    public static <T> CompletableFuture<T> retryAsync(
        Supplier<CompletableFuture<T>> callback,
        Predicate<Throwable> exceptionPredicate,
        Duration maxDelayBetweenRetries,
        ScheduledExecutorService executor) {
        return retryAsync(callback, exceptionPredicate, maxDelayBetweenRetries, null, executor);
    }

    public static <T> CompletableFuture<T> retryAsync(
        Supplier<CompletableFuture<T>> callback,
        Predicate<Throwable> exceptionPredicate,
        Duration maxDelayBetweenRetries,
        @Nullable Duration timeout,
        ScheduledExecutorService executor) {
        CompletableFuture<T> future = new CompletableFuture<>();
        retryAsync(callback, exceptionPredicate, maxDelayBetweenRetries, timeout, System.currentTimeMillis(), 1, future, executor);
        return future;
    }

    private static <T> CompletableFuture<T> retryAsync(
        Supplier<CompletableFuture<T>> callback,
        Predicate<Throwable> exceptionPredicate,
        Duration maxDelayBetweenRetries,
        Duration timeout,
        long startTime,
        int attempt,
        CompletableFuture<T> future,
        ScheduledExecutorService executor) {
        callback.get().whenComplete((r1, e1) -> {
            if (e1 != null) {
                if (!exceptionPredicate.test(e1)) {
                    future.completeExceptionally(e1);
                    return;
                }

                long currentTime = System.currentTimeMillis();
                if (timeout != null && currentTime - startTime > timeout.toMillis()) {
                    future.completeExceptionally(new PrimitiveException.Timeout(String.format("timed out after %d milliseconds", currentTime - startTime)));
                } else {
                    executor.schedule(() ->
                            retryAsync(callback, exceptionPredicate, maxDelayBetweenRetries, timeout, startTime, attempt + 1, future, executor),
                        (int) Math.min(Math.pow(2, attempt) * BASE_DELAY.toMillis(), maxDelayBetweenRetries.toMillis()), TimeUnit.MILLISECONDS);
                }
            } else {
                future.complete(r1);
            }
        });
        return future;
    }

    public static <T> CompletableFuture<T> retryAsync(
        Supplier<CompletableFuture<T>> callback,
        Predicate<Throwable> exceptionPredicate,
        int maxRetries,
        Duration maxDelayBetweenRetries,
        ScheduledExecutorService executor) {
        CompletableFuture<T> future = new CompletableFuture<>();
        retryAsync(callback, exceptionPredicate, maxRetries, maxDelayBetweenRetries, System.currentTimeMillis(), 1, future, executor);
        return future;
    }

    private static <T> CompletableFuture<T> retryAsync(
        Supplier<CompletableFuture<T>> callback,
        Predicate<Throwable> exceptionPredicate,
        int maxRetries,
        Duration maxDelayBetweenRetries,
        long startTime,
        int attempt,
        CompletableFuture<T> future,
        ScheduledExecutorService executor) {
        callback.get().whenComplete((r1, e1) -> {
            if (e1 != null) {
                if (!exceptionPredicate.test(e1)) {
                    future.completeExceptionally(e1);
                } else if (attempt == maxRetries) {
                    future.completeExceptionally(new PrimitiveException.Timeout(String.format("timed out after %d milliseconds", System.currentTimeMillis() - startTime)));
                } else {
                    executor.schedule(() ->
                            retryAsync(callback, exceptionPredicate, maxRetries, maxDelayBetweenRetries, startTime, attempt + 1, future, executor),
                        (int) Math.min(Math.pow(2, attempt) * BASE_DELAY.toMillis(), maxDelayBetweenRetries.toMillis()), TimeUnit.MILLISECONDS);
                }
            } else {
                future.complete(r1);
            }
        });
        return future;
    }

    /**
     * Returns a function that retries execution on failure.
     *
     * @param base                   base function
     * @param exceptionClass         type of exception for which to retry
     * @param maxRetries             max number of retries before giving up
     * @param maxDelayBetweenRetries max delay between successive retries. The actual delay is randomly picked from
     *                               the interval (0, maxDelayBetweenRetries]
     * @param <U>                    type of function input
     * @param <V>                    type of function output
     * @return function
     */
    public static <U, V> Function<U, V> retryable(Function<U, V> base,
                                                  Class<? extends Throwable> exceptionClass,
                                                  int maxRetries,
                                                  int maxDelayBetweenRetries) {
        return new RetryingFunction<>(base, exceptionClass, maxRetries, maxDelayBetweenRetries);
    }

    /**
     * Returns a Supplier that retries execution on failure.
     *
     * @param base                   base supplier
     * @param exceptionClass         type of exception for which to retry
     * @param maxRetries             max number of retries before giving up
     * @param maxDelayBetweenRetries max delay between successive retries. The actual delay is randomly picked from
     *                               the interval (0, maxDelayBetweenRetries]
     * @param <V>                    type of supplied result
     * @return supplier
     */
    public static <V> Supplier<V> retryable(Supplier<V> base,
                                            Class<? extends Throwable> exceptionClass,
                                            int maxRetries,
                                            int maxDelayBetweenRetries) {
        return () -> new RetryingFunction<>(v -> base.get(),
            exceptionClass,
            maxRetries,
            maxDelayBetweenRetries).apply(null);
    }

    /**
     * Suspends the current thread for a random number of millis between 0 and
     * the indicated limit.
     *
     * @param ms max number of millis
     */
    public static void randomDelay(int ms) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(ms));
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }
    }

    /**
     * Suspends the current thread for a specified number of millis and nanos.
     *
     * @param ms    number of millis
     * @param nanos number of nanos
     */
    public static void delay(int ms, int nanos) {
        try {
            Thread.sleep(ms, nanos);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }
    }

    private Retries() {
    }

}