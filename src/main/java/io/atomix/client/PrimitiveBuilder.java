// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;


import io.grpc.Channel;

import java.util.HashMap;
import java.util.Map;
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
    protected final String name;
    protected final Channel channel;
    protected boolean readOnly;
    protected Map<String, String> tags = new HashMap<>();

    protected PrimitiveBuilder(String name, Channel channel) {
        this.name = checkNotNull(name, "primitive name cannot be null");
        this.channel = checkNotNull(channel, "primitive channel cannot be null");
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
    protected Channel channel() {
        return channel;
    }

    /**
     * Returns the primitive tags.
     *
     * @return the primitive tags
     */
    protected Map<String, String> tags() {
        return tags;
    }

    /**
     * Sets the primitive tags.
     *
     * @param tags the primitive tags
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withTags(Map<String, String> tags) {
        this.tags.putAll(tags);
        return (B) this;
    }

    /**
     * Adds a tag to the primitive.
     *
     * @param key the tag key
     * @param value the tag value
     * @return the primitive builder
     */
    @SuppressWarnings("unchecked")
    public B withTag(String key, String value) {
        tags.put(key, value);
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
}
