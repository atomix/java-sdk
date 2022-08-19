package io.atomix.client.value.impl;

import com.google.common.base.Throwables;
import io.atomix.client.Cancellable;
import io.atomix.client.PrimitiveException;
import io.atomix.client.Synchronous;
import io.atomix.client.time.Versioned;
import io.atomix.client.value.AsyncAtomicValue;
import io.atomix.client.value.AtomicValue;
import io.atomix.client.value.AtomicValueEventListener;

import java.util.ConcurrentModificationException;
import java.util.concurrent.*;

/**
 * Default implementation for a {@code AtomicValue} backed by a {@link AsyncAtomicValue}.
 *
 * @param <V> value type
 */
public class BlockingAtomicValue<V> extends Synchronous<AsyncAtomicValue<V>> implements AtomicValue<V> {
    private final AsyncAtomicValue<V> asyncValue;
    private final long operationTimeoutMillis;

    public BlockingAtomicValue(AsyncAtomicValue<V> asyncValue, long operationTimeoutMillis) {
        super(asyncValue);
        this.asyncValue = asyncValue;
        this.operationTimeoutMillis = operationTimeoutMillis;
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

    protected <T> T complete(CompletableFuture<T> future) {
        try {
            return future.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrimitiveException.Interrupted();
        } catch (TimeoutException e) {
            throw new PrimitiveException.Timeout();
        } catch (ExecutionException e) {
            Throwable cause = Throwables.getRootCause(e);
            if (cause instanceof PrimitiveException) {
                throw (PrimitiveException) cause;
            } else if (cause instanceof ConcurrentModificationException) {
                throw (ConcurrentModificationException) cause;
            } else {
                throw new PrimitiveException(cause);
            }
        }
    }
}
