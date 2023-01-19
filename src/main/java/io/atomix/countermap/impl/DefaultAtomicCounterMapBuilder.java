// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.countermap.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.countermap.v1.CounterMapGrpc;
import io.atomix.api.countermap.v1.CreateRequest;
import io.atomix.countermap.AsyncAtomicCounterMap;
import io.atomix.countermap.AtomicCounterMap;
import io.atomix.countermap.AtomicCounterMapBuilder;

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
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        DefaultAsyncAtomicCounterMap rawCounterMap = new DefaultAsyncAtomicCounterMap(name(), this.stub, this.executorService);
        return retry(CounterMapGrpc.CounterMapStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> new TranscodingAsyncAtomicCounterMap<>(rawCounterMap, keyEncoder))
                .thenApply(AsyncAtomicCounterMap::sync);
    }
}
