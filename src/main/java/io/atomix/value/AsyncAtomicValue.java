package io.atomix.value;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.AsyncPrimitive;
import io.atomix.Cancellable;
import io.atomix.time.Versioned;
import io.atomix.value.impl.BlockingAtomicValue;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Distributed version of java.util.concurrent.atomic.AtomicReference.
 * <p>
 * All methods of this interface return a {@link CompletableFuture future} immediately
 * after a successful invocation. The operation itself is executed asynchronous and
 * the returned future will be {@link CompletableFuture#complete completed} when the
 * operation finishes.
 *
 * @param <V> value type
 */
public interface AsyncAtomicValue<V> extends AsyncPrimitive<AsyncAtomicValue<V>, AtomicValue<V>> {

    /**
     * Gets the current value.
     *
     * @return current value
     */
    CompletableFuture<Versioned<V>> get();

    /**
     * Sets to the given value.
     *
     * @param value new value
     * @return previous versioned value
     */
    CompletableFuture<Versioned<V>> set(V value);

    /**
     * Sets to the given value.
     *
     * @param value new value
     * @return previous versioned value
     */
    CompletableFuture<Versioned<V>> set(V value, long version);

    /**
     * Registers the specified listener to be notified whenever the atomic value is updated.
     *
     * @param listener listener to notify about events
     * @return CompletableFuture that will be completed when the operation finishes
     */
    default CompletableFuture<Cancellable> listen(AtomicValueEventListener<V> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever the atomic value is updated.
     *
     * @param listener listener to notify about events
     * @return CompletableFuture that will be completed when the operation finishes
     */
    CompletableFuture<Cancellable> listen(AtomicValueEventListener<V> listener, Executor executor);

    @Override
    default AtomicValue<V> sync(Duration operationTimeout) {
        return new BlockingAtomicValue<>(this, operationTimeout);
    }
}
