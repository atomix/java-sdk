// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import io.atomix.AtomixChannel;
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
        return new DefaultAsyncAtomicMap(name(), MapGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(map -> new TranscodingAsyncAtomicMap<>(map, keyEncoder, keyDecoder, valueEncoder, valueDecoder))
            .thenApply(AsyncAtomicMap::sync);
    }
}
