// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.set.DistributedSet;

/**
 * Distributed map.
 */
public interface DistributedMap<K, V> extends SyncPrimitive, Map<K, V> {

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     */
    default Cancellable listen(MapEventListener<K, V> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     * @param executor executor to use for handling incoming map events
     */
    Cancellable listen(MapEventListener<K, V> listener, Executor executor);

    @Override
    DistributedSet<K> keySet();

    @Override
    DistributedSet<Entry<K, V>> entrySet();

    @Override
    DistributedCollection<V> values();

    @Override
    AsyncDistributedMap<K, V> async();
}