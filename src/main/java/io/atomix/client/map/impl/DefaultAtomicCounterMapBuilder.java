// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.countermap.v1.CounterMapGrpc;
import io.atomix.api.runtime.multimap.v1.MultiMapGrpc;
import io.atomix.client.map.AsyncAtomicCounterMap;
import io.atomix.client.map.AsyncDistributedMultimap;
import io.atomix.client.map.AtomicCounterMap;
import io.atomix.client.map.AtomicCounterMapBuilder;
import io.atomix.client.map.DistributedMultimap;
import io.atomix.client.map.DistributedMultimapBuilder;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicCounterMapBuilder<K> extends AtomicCounterMapBuilder<K> {
    public DefaultAtomicCounterMapBuilder(String name, Channel channel, ScheduledExecutorService executorService) {
        super(name, channel, executorService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicCounterMap<K>> buildAsync() {
        return new DefaultAsyncAtomicCounterMap(name(), CounterMapGrpc.newStub(channel()), executor())
            .create(tags())
            .thenApply(multimap -> new TranscodingAsyncAtomicCounterMap<K, String>(multimap,
                key -> BaseEncoding.base64().encode(serializer.encode(key))))
            .thenApply(AsyncAtomicCounterMap::sync);
    }
}
