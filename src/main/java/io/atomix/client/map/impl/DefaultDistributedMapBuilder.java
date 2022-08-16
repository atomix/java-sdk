// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.client.map.*;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultDistributedMapBuilder<K, V> extends DistributedMapBuilder<K, V> {
    public DefaultDistributedMapBuilder(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<DistributedMap<K, V>> buildAsync() {
        return new DefaultAsyncDistributedMap(name(), channel())
                .create(tags())
                .thenApply(map -> new TranscodingAsyncDistributedMap<K, V, String, byte[]>(map,
                        key -> BaseEncoding.base64().encode(keySerializer.serialize(key)),
                        key -> keySerializer.deserialize(BaseEncoding.base64().decode(key)),
                        valueSerializer::serialize,
                        valueSerializer::deserialize))
                .thenApply(AsyncDistributedMap::sync);
    }
}
