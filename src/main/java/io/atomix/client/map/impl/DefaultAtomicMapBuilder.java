// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.client.map.AsyncAtomicMap;
import io.atomix.client.map.AtomicMap;
import io.atomix.client.map.AtomicMapBuilder;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicMapBuilder<K, V> extends AtomicMapBuilder<K, V> {
    public DefaultAtomicMapBuilder(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicMap<K, V>> buildAsync() {
        return new DefaultAsyncAtomicMap(name(), channel())
                .create(tags())
                .thenApply(map -> new TranscodingAsyncAtomicMap<K, V, String, byte[]>(map,
                        key -> BaseEncoding.base64().encode(serializer.encode(key)),
                        key -> serializer.decode(BaseEncoding.base64().decode(key)),
                        serializer::encode,
                        serializer::decode))
                .thenApply(AsyncAtomicMap::sync);
    }
}
