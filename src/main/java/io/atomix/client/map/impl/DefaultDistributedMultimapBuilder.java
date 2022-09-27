// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.multimap.v1.MultiMapGrpc;
import io.atomix.client.map.AsyncDistributedMultimap;
import io.atomix.client.map.DistributedMultimap;
import io.atomix.client.map.DistributedMultimapBuilder;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Atomic counter proxy builder.
 */
public class DefaultDistributedMultimapBuilder<K, V> extends DistributedMultimapBuilder<K, V> {
    public DefaultDistributedMultimapBuilder(String name, Channel channel, ScheduledExecutorService executorService) {
        super(name, channel, executorService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<DistributedMultimap<K, V>> buildAsync() {
        return new DefaultAsyncDistributedMultimap(name(), MultiMapGrpc.newStub(channel()), executor())
            .create(tags())
            .thenApply(multimap -> new TranscodingAsyncDistributedMultimap<K, V, String, String>(multimap,
                key -> BaseEncoding.base64().encode(serializer.encode(key)),
                key -> serializer.decode(BaseEncoding.base64().decode(key)),
                value -> value != null ? BaseEncoding.base64().encode(serializer.encode(value)) : null,
                bytes -> bytes != null ? serializer.decode(BaseEncoding.base64().decode(bytes)) : null))
            .thenApply(AsyncDistributedMultimap::sync);
    }
}
