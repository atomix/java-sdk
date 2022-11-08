// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
    private final AtomixChannel channel;
    private String name;
    private final Set<String> tags = new HashSet<>();

    protected PrimitiveBuilder(AtomixChannel channel) {
        this.channel = checkNotNull(channel, "primitive channel cannot be null");
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
}
