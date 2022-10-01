package io.atomix.value.impl;

import io.atomix.Cancellable;
import io.atomix.Synchronous;
import io.atomix.time.Versioned;
import io.atomix.value.AsyncAtomicValue;
import io.atomix.value.AtomicValue;
import io.atomix.value.AtomicValueEventListener;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Default implementation for a {@code AtomicValue} backed by a {@link AsyncAtomicValue}.
 *
 * @param <V> value type
 */
public class BlockingAtomicValue<V> extends Synchronous<AtomicValue<V>, AsyncAtomicValue<V>> implements AtomicValue<V> {
    private final AsyncAtomicValue<V> asyncValue;

    public BlockingAtomicValue(AsyncAtomicValue<V> asyncValue, Duration operationTimeout) {
        super(asyncValue, operationTimeout);
        this.asyncValue = asyncValue;
    }

    @Override
    public Versioned<V> get() {
        return complete(asyncValue.get());
    }

    @Override
    public Versioned<V> set(V value) {
        return complete(asyncValue.set(value));
    }

    @Override
    public Versioned<V> set(V value, long version) {
        return complete(asyncValue.set(value, version));
    }

    @Override
    public Cancellable listen(AtomicValueEventListener<V> listener, Executor executor) {
        return complete(asyncValue.listen(listener, executor));
    }

    @Override
    public AsyncAtomicValue<V> async() {
        return asyncValue;
    }
}
