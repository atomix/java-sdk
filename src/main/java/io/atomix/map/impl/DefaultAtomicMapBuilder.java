// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.map.v1.CreateRequest;
import io.atomix.api.map.v1.MapGrpc;
import io.atomix.map.AsyncAtomicMap;
import io.atomix.map.AtomicMap;
import io.atomix.map.AtomicMapBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicMapBuilder<K, V> extends AtomicMapBuilder<K, V> {

    public DefaultAtomicMapBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicMap<K, V>> buildAsync() {
        if (keyEncoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("keyEncoder cannot be null"));
        }
        if (keyDecoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("keyDecoder cannot be null"));
        }
        if (valueEncoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("valueEncoder cannot be null"));
        }
        if (valueDecoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("valueDecoder cannot be null"));
        }
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        DefaultAsyncAtomicMap rawMap = new DefaultAsyncAtomicMap(name(), this.stub, this.executorService);
        return retry(MapGrpc.MapStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> {
                    // Underlay map which wraps the gRPC "map"
                    AsyncAtomicMap<K, V> map = new TranscodingAsyncAtomicMap<>(
                            rawMap, keyEncoder, keyDecoder, valueEncoder, valueDecoder);
                    // If config is enabled we further decorate with the caching wrap
                    if (response.hasConfig() && response.getConfig().hasCache() &&
                            response.getConfig().getCache().getEnabled()) {
                        map = new CachingAsyncAtomicMap<>(map, response.getConfig().getCache().getSize());
                    }
                    return map.sync();
                });
    }



}
