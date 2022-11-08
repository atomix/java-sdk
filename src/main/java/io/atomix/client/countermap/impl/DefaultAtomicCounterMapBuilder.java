// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.countermap.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.countermap.v1.CounterMapGrpc;
import io.atomix.client.AtomixChannel;
import io.atomix.client.countermap.AsyncAtomicCounterMap;
import io.atomix.client.countermap.AtomicCounterMap;
import io.atomix.client.countermap.AtomicCounterMapBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicCounterMapBuilder<K> extends AtomicCounterMapBuilder<K> {
    public DefaultAtomicCounterMapBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    public CompletableFuture<AtomicCounterMap<K>> buildAsync() {
        if (keyEncoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("keyEncoder cannot be null"));
        }
        return new DefaultAsyncAtomicCounterMap(name(), CounterMapGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(multimap -> new TranscodingAsyncAtomicCounterMap<>(multimap, keyEncoder))
            .thenApply(AsyncAtomicCounterMap::sync);
    }
}
