// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix;

import io.atomix.api.runtime.v1.PrimitiveID;
import io.atomix.util.concurrent.Retries;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract builder for distributed primitives.
 *
 * @param <B> builder type
 * @param <P> primitive type
 */
public abstract class PrimitiveBuilder<B extends PrimitiveBuilder<B, P, T>, P extends SyncPrimitive, T> {
    private static final Duration MAX_DELAY_BETWEEN_RETRIES = Duration.ofSeconds(5);
    private final AtomixChannel channel;
    private String name;
    private final Set<String> tags = new HashSet<>();
    protected final T stub;
    protected final ScheduledExecutorService executorService;

    protected PrimitiveBuilder(AtomixChannel channel, T stub, ScheduledExecutorService executorService) {
        this.channel = checkNotNull(channel, "primitive channel cannot be null");
        this.stub = checkNotNull(stub, "primitive stub cannot be null");
        this.executorService = checkNotNull(executorService, "primitive executor cannot be null");
    }

    /**
     * Sets the primitive name.
     *
     * @param name the primitive name
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withName(String name) {
        this.name = checkNotNull(name, "primitive name cannot be null");
        return (B) this;
    }

    /**
     * Returns the primitive name.
     *
     * @return the primitive name
     */
    protected String name() {
        return name;
    }

    /**
     * Returns the service channel.
     *
     * @return the service channel
     */
    protected AtomixChannel channel() {
        return channel;
    }

    /**
     * Returns the primitive tags.
     *
     * @return the primitive tags
     */
    protected Set<String> tags() {
        return tags;
    }

    /**
     * Return the primitive id.
     *
     * @return the primitive id
     */
    protected final PrimitiveID id() {
        return PrimitiveID.newBuilder()
                .setName(name())
                .build();
    }

    /**
     * Sets the primitive tags.
     *
     * @param tags the primitive tags
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withTags(String... tags) {
        this.tags.addAll(Arrays.asList(tags));
        return (B) this;
    }

    /**
     * Sets the primitive tags.
     *
     * @param tags the primitive tags
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return (B) this;
    }

    /**
     * Adds a tag to the primitive.
     *
     * @param tag the tag to add
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withTag(String tag) {
        tags.add(tag);
        return (B) this;
    }

    /**
     * Builds a new instance of the primitive.
     * <p>
     * The returned instance will be distinct from all other instances of the same primitive on this node, with a
     * distinct session, ordering guarantees, memory, etc.
     *
     * @return a new instance of the primitive
     */
    public P build() {
        try {
            return buildAsync().join();
        } catch (Exception e) {
            if (e instanceof CompletionException && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * Builds a new instance of the primitive asynchronously.
     * <p>
     * The returned instance will be distinct from all other instances of the same primitive on this node, with a
     * distinct session, ordering guarantees, memory, etc.
     *
     * @return asynchronous distributed primitive
     */
    public abstract CompletableFuture<P> buildAsync();

    private <U, V> CompletableFuture<V> execute(StubMethodCall<T, U, V> callback, U request) {
        CompletableFuture<V> future = new CompletableFuture<>();
        StreamObserver<V> responseObserver = new StreamObserver<V>() {
            @Override
            public void onNext(V response) {
                future.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {

            }
        };
        callback.call(stub, request, responseObserver);
        return future;
    }

    protected <U, V> CompletableFuture<V> retry(StubMethodCall<T, U, V> callback, U request) {
        return Retries.retryAsync(
                () -> execute(callback, request),
                t -> Status.fromThrowable(t).getCode() == Status.UNAVAILABLE.getCode(),
                MAX_DELAY_BETWEEN_RETRIES,
                executorService);
    }

    protected interface StubMethodCall<T, U, V> {
        void call(T stub, U request, StreamObserver<V> streamObserver);
    }
}
