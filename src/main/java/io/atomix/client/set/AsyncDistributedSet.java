// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set;

import io.atomix.client.SyncPrimitive;
import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.set.impl.BlockingDistributedSet;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * A distributed collection designed for holding unique elements.
 * <p>
 * All methods of {@code AsyncDistributedSet} immediately return a {@link java.util.concurrent.CompletableFuture future}.
 * The returned future will be {@link java.util.concurrent.CompletableFuture#complete completed} when the operation
 * completes.
 *
 * @param <E> set entry type
 */
public interface AsyncDistributedSet<E> extends AsyncDistributedCollection<E> {
    /**
     * Adds the specified element to this collection if it is not already present (optional operation).
     *
     * @param element element to add
     * @return {@code true} if this collection did not already contain the specified element.
     */
    CompletableFuture<Boolean> add(E element, Duration ttl);

    @Override
    default DistributedSet<E> sync() {
        return sync(SyncPrimitive.DEFAULT_OPERATION_TIMEOUT);
    }

    @Override
    default DistributedSet<E> sync(Duration operationTimeout) {
        return new BlockingDistributedSet<>(this, operationTimeout);
    }
}
