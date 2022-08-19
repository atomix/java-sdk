package io.atomix.client.value;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.time.Versioned;

import java.util.concurrent.Executor;

/**
 * Distributed version of java.util.concurrent.atomic.AtomicReference.
 *
 * @param <V> value type
 */
public interface AtomicValue<V> extends SyncPrimitive {

    /**
     * Gets the current value.
     *
     * @return current value
     */
    Versioned<V> get();

    /**
     * Sets to the given value.
     *
     * @param value new value
     * @return previous versioned value
     */
    Versioned<V> set(V value);

    /**
     * Sets to the given value.
     *
     * @param value new value
     * @return previous versioned value
     */
    Versioned<V> set(V value, long version);

    /**
     * Registers the specified listener to be notified whenever the atomic value is updated.
     *
     * @param listener listener to notify about events
     */
    default Cancellable listen(AtomicValueEventListener<V> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever the atomic value is updated.
     *
     * @param listener listener to notify about events
     */
    Cancellable listen(AtomicValueEventListener<V> listener, Executor executor);

    @Override
    AsyncAtomicValue<V> async();
}