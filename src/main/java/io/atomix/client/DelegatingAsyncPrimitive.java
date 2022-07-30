package io.atomix.client;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for primitive delegates.
 */
public abstract class DelegatingAsyncPrimitive<T extends AsyncPrimitive> implements AsyncPrimitive {
    private final T primitive;

    public DelegatingAsyncPrimitive(T primitive) {
        this.primitive = checkNotNull(primitive);
    }

    /**
     * Returns the delegate primitive.
     *
     * @return the underlying primitive
     */
    protected T delegate() {
        return primitive;
    }

    @Override
    public String name() {
        return primitive.name();
    }

    @Override
    public void addStateChangeListener(Consumer<PrimitiveState> listener) {
        primitive.addStateChangeListener(listener);
    }

    @Override
    public void removeStateChangeListener(Consumer<PrimitiveState> listener) {
        primitive.removeStateChangeListener(listener);
    }

    @Override
    public CompletableFuture<Void> close() {
        return primitive.close();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("delegate", primitive)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(primitive);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DelegatingAsyncPrimitive
                && primitive.equals(((DelegatingAsyncPrimitive) other).primitive);
    }
}