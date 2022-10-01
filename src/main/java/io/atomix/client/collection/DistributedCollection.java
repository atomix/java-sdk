// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.collection;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.iterator.SyncIterable;
import io.atomix.client.iterator.SyncIterator;

import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * Distributed collection.
 */
public interface DistributedCollection<E> extends SyncPrimitive<DistributedCollection<E>, AsyncDistributedCollection<E>>, SyncIterable<E>, Collection<E> {
    @Override
    SyncIterator<E> iterator();

    /**
     * Registers the specified listener to be notified whenever
     * the collection is updated.
     *
     * @param listener listener to notify about collection update events
     * @return a cancellable to be used to cancel the listener
     */
    default Cancellable listen(CollectionEventListener<E> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever
     * the collection is updated.
     *
     * @param listener listener to notify about collection update events
     * @return a cancellable to be used to cancel the listener
     */
    Cancellable listen(CollectionEventListener<E> listener, Executor executor);

    @Override
    AsyncDistributedCollection<E> async();

}