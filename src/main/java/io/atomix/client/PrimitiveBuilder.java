// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;


import io.atomix.client.utils.ThreadContext;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract builder for distributed primitives.
 *
 * @param <B> builder type
 * @param <P> primitive type
 */
public abstract class PrimitiveBuilder<B extends PrimitiveBuilder<B, P>, P extends SyncPrimitive> {
    protected final String primitiveName;
    protected boolean readOnly;
    protected final Channel serviceChannel;
    protected final ThreadContext threadContext;

    protected PrimitiveBuilder(String primitiveName, Channel serviceChannel, ThreadContext threadContext) {
        this.primitiveName = checkNotNull(primitiveName,
                                          "primitive name cannot be null");
        this.serviceChannel = checkNotNull(serviceChannel,
                                                  "primitive channel cannot be null");
        this.threadContext = checkNotNull(threadContext,
                                           "thread context cannot be null");
    }

    /**
     * Returns the primitive name.
     *
     * @return the primitive name
     */
    protected String getPrimitiveName() {
        return primitiveName;
    }

    /**
     * Returns the service channel.
     *
     * @return the service channel
     */
    protected Channel getServiceChannel() {
        return serviceChannel;
    }

    /**
     * Returns the thread context.
     *
     * @return the thread context
     */
    protected ThreadContext getThreadContext() {
        return threadContext;
    }

    /**
     * Sets the primitive to read-only.
     *
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withReadOnly() {
        return withReadOnly(true);
    }

    /**
     * Sets whether the primitive is read-only.
     *
     * @param readOnly whether the primitive is read-only
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
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

    /**
     * Gets or builds a singleton instance of the primitive.
     * <p>
     * The returned primitive will be shared by all {@code get()} calls for the named primitive. If no instance has yet
     * been constructed, the instance will be built from this builder's configuration.
     *
     * @return a singleton instance of the primitive
     */
    public P get() {
        try {
            return getAsync().join();
        } catch (Exception e) {
            if (e instanceof CompletionException && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * Gets or builds a singleton instance of the primitive asynchronously.
     * <p>
     * The returned primitive will be shared by all {@code get()} calls for the named primitive. If no instance has yet
     * been constructed, the instance will be built from this builder's configuration.
     *
     * @return a singleton instance of the primitive
     */
    public CompletableFuture<P> getAsync() {
        return null;
    }
}
