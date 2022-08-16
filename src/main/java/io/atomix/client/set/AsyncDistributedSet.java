// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set;

import io.atomix.client.DistributedPrimitive;
import io.atomix.client.collection.AsyncDistributedCollection;

import java.time.Duration;

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
    @Override
    default DistributedSet<E> sync() {
        return sync(Duration.ofMillis(DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS));
    }

    @Override
    DistributedSet<E> sync(Duration operationTimeout);
}