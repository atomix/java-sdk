// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;


import io.grpc.Channel;
import io.grpc.Context;

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
    protected final String applicationName;
    protected final String sessionId;
    protected boolean readOnly;
    protected final Channel serviceChannel;
    protected final Context context;
    protected final PrimitiveManagementService primitiveManagementService;

    protected PrimitiveBuilder(String primitiveName, String applicationName, String sessionId,
                               Channel serviceChannel, Context context,
                               PrimitiveManagementService primitiveManagementService) {
        this.primitiveName = checkNotNull(primitiveName, "primitive name cannot be null");
        this.applicationName = checkNotNull(applicationName, "application name cannot be null");
        this.sessionId = checkNotNull(sessionId, "session id cannot be null");
        this.serviceChannel = checkNotNull(serviceChannel, "primitive channel cannot be null");
        this.context = checkNotNull(context, "context cannot be null");
        this.primitiveManagementService = checkNotNull(primitiveManagementService,
                                                       "primitive management service cannot be null ");
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
     * Returns the application name.
     *
     * @return the application name
     */
    protected String getApplicationName() {
        return applicationName;
    }

    /**
     * Returns the session id.
     *
     * @return the session id
     */
    protected String getSessionId() {
        return sessionId;
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
     * Returns the gRPC context.
     *
     * @return the gRPC context
     */
    protected Context getContext() {
        return context;
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
        return this.primitiveManagementService.getPrimitive(primitiveName, this::buildAsync);
    }
}
