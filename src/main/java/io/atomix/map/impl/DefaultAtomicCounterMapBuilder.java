// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.countermap.v1.CounterMapGrpc;
import io.atomix.AtomixChannel;
import io.atomix.map.AsyncAtomicCounterMap;
import io.atomix.map.AtomicCounterMap;
import io.atomix.map.AtomicCounterMapBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicCounterMapBuilder<K> extends AtomicCounterMapBuilder<K> {
    public DefaultAtomicCounterMapBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicCounterMap<K>> buildAsync() {
        return new DefaultAsyncAtomicCounterMap(name(), CounterMapGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(multimap -> new TranscodingAsyncAtomicCounterMap<K, String>(multimap,
                key -> BaseEncoding.base64().encode(serializer.encode(key))))
            .thenApply(AsyncAtomicCounterMap::sync);
    }
}
