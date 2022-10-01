package io.atomix.client.value.impl;

import io.atomix.client.Cancellable;
import io.atomix.client.Synchronous;
import io.atomix.client.time.Versioned;
import io.atomix.client.value.AsyncAtomicValue;
import io.atomix.client.value.AtomicValue;
import io.atomix.client.value.AtomicValueEventListener;

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
