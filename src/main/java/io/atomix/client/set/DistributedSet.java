// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set;

import io.atomix.client.collection.DistributedCollection;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A distributed collection designed for holding unique elements.
 *
 * @param <E> set entry type
 */
public interface DistributedSet<E> extends DistributedCollection<E>, Set<E> {
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