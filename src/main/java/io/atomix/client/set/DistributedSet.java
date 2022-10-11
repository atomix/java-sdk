// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set;

import io.atomix.client.AtomixChannel;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.set.impl.DefaultDistributedSetBuilder;

import java.time.Duration;
import java.util.Set;

/**
 * A distributed collection designed for holding unique elements.
 *
 * @param <E> set entry type
 */
public interface DistributedSet<E> extends DistributedCollection<E>, Set<E> {

    /**
     * Returns a new DistributedSet builder.
     *
     * @param channel the AtomixChannel
     * @return the DistributedSet builder
     */
    static <E> DistributedSetBuilder<E> builder(AtomixChannel channel) {
        return new DefaultDistributedSetBuilder<>(channel);
    }

    /**
     * Adds the specified element to this collection if it is not already present (optional operation).
     *
     * @param element element to add
     * @return {@code true} if this collection did not already contain the specified element.
     */
    boolean add(E element, Duration ttl);

    @Override
    AsyncDistributedSet<E> async();
}