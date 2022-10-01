// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.map.v1.MapGrpc;
import io.atomix.AtomixChannel;
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
        return new DefaultAsyncAtomicMap(name(), MapGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(map -> new TranscodingAsyncAtomicMap<K, V, String, byte[]>(map,
                key -> BaseEncoding.base64().encode(serializer.encode(key)),
                key -> serializer.decode(BaseEncoding.base64().decode(key)),
                value -> value != null ? serializer.encode(value) : null,
                bytes -> bytes != null && bytes.length > 0 ? serializer.decode(bytes) : null))
            .thenApply(AsyncAtomicMap::sync);
    }
}
