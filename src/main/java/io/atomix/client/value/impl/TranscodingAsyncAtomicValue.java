package io.atomix.client.value.impl;

import io.atomix.client.Cancellable;
import io.atomix.client.DelegatingAsyncPrimitive;
import io.atomix.client.time.Versioned;
import io.atomix.client.value.AsyncAtomicValue;
import io.atomix.client.value.AtomicValue;
import io.atomix.client.value.AtomicValueEvent;
import io.atomix.client.value.AtomicValueEventListener;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Transcoding async atomic value.
 */
public class TranscodingAsyncAtomicValue<V1, V2> extends DelegatingAsyncPrimitive<AsyncAtomicValue<V2>> implements AsyncAtomicValue<V1> {
    private final AsyncAtomicValue<V2> backingValue;
    private final Function<V1, V2> valueEncoder;
    private final Function<V2, V1> valueDecoder;

    public TranscodingAsyncAtomicValue(AsyncAtomicValue<V2> backingValue, Function<V1, V2> valueEncoder, Function<V2, V1> valueDecoder) {
        super(backingValue);
        this.backingValue = backingValue;
        this.valueEncoder = v -> v != null ? valueEncoder.apply(v) : null;
        this.valueDecoder = v -> v != null ? valueDecoder.apply(v) : null;
    }

    @Override
    public CompletableFuture<Versioned<V1>> get() {
        return backingValue.get()
            .thenApply(versioned -> versioned != null
                ? new Versioned<>(valueDecoder.apply(versioned.value()), versioned.version()) : null);
    }

    @Override
    public CompletableFuture<Versioned<V1>> set(V1 value) {
        return backingValue.set(valueEncoder.apply(value))
            .thenApply(versioned -> versioned != null
                ? new Versioned<>(valueDecoder.apply(versioned.value()), versioned.version()) : null);
    }

    @Override
    public CompletableFuture<Versioned<V1>> set(V1 value, long version) {
        return backingValue.set(valueEncoder.apply(value), version)
            .thenApply(versioned -> versioned != null
                ? new Versioned<>(valueDecoder.apply(versioned.value()), versioned.version()) : null);
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicValueEventListener<V1> listener, Executor executor) {
        return backingValue.listen(event -> new AtomicValueEvent<>(
            AtomicValueEvent.Type.UPDATE,
            valueDecoder.apply(event.newValue()),
            valueDecoder.apply(event.oldValue())), executor);
    }

    @Override
    public AtomicValue<V1> sync(Duration operationTimeout) {
        return new BlockingAtomicValue<>(this, operationTimeout.toMillis());
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("backingValue", backingValue)
            .toString();
    }
}
