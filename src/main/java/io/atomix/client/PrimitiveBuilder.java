// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.utils.serializer.Serializer;

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
    protected final PrimitiveId primitiveId;
    protected boolean readOnly;
    protected Serializer serializer;
    protected final PrimitiveManagementService managementService;

    protected PrimitiveBuilder(PrimitiveId primitiveId, PrimitiveManagementService managementService) {
        this.primitiveId = checkNotNull(primitiveId, "primitiveId cannot be null");
        this.managementService = checkNotNull(managementService, "managementService cannot be null");
    }

    /**
     * Returns the namespaced primitive id.
     *
     * @return the namespaced primitive id
     */
    protected PrimitiveId getPrimitiveId() {
        return primitiveId;
    }

    /**
     * Sets the primitive serializer.
     *
     * @param serializer the primitive serializer
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withSerializer(Serializer serializer) {
        this.serializer = checkNotNull(serializer);
        return (B) this;
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
     * Returns the protocol serializer.
     *
     * @return the protocol serializer
     */
    protected Serializer serializer() {
        return serializer;
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
        return managementService.getPrimitive(primitiveId, this::buildAsync);
    }
}
