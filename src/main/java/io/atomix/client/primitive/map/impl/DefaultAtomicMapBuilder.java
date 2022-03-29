// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.map.impl;

import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.PrimitiveManagementService;
import io.atomix.client.primitive.map.AsyncAtomicMap;
import io.atomix.client.primitive.map.AtomicMap;
import io.atomix.client.primitive.map.AtomicMapBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Default {@link AsyncAtomicMap} builder.
 *
 * @param <K> type for map key
 * @param <V> type for map value
 */
public class DefaultAtomicMapBuilder<K, V> extends AtomicMapBuilder<K, V> {
    public DefaultAtomicMapBuilder(PrimitiveId id, PrimitiveManagementService managementService) {
        super(id, managementService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicMap<K, V>> buildAsync() {



        return managementService.getPartitionService().getPartitionGroup(group)
                .thenCompose(group -> {
                    Map<Integer, AsyncAtomicMap<String, byte[]>> partitions = group.getPartitions().stream()
                            .map(partition -> Maps.immutableEntry(partition.id(), new DefaultAsyncAtomicMap(getName(), partition, managementService.getThreadFactory().createContext(), sessionTimeout)))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    return new PartitionedAsyncAtomicMap(name, partitions, partitioner).connect();
                })
                .thenApply(rawMap -> {
                    Serializer serializer = serializer();
                    return new TranscodingAsyncAtomicMap<K, V, String, byte[]>(
                            rawMap,
                            key -> BaseEncoding.base16().encode(serializer.encode(key)),
                            string -> serializer.decode(BaseEncoding.base16().decode(string)),
                            value -> serializer.encode(value),
                            bytes -> serializer.decode(bytes));
                })
                .<AsyncAtomicMap<K, V>>thenApply(map -> {
                    if (cacheEnabled) {
                        return new CachingAsyncAtomicMap<>(map, cacheSize);
                    }
                    return map;
                })
                .thenApply(map -> {
                    if (readOnly) {
                        return new UnmodifiableAsyncAtomicMap<>(map);
                    }
                    return map;
                })
                .thenApply(AsyncAtomicMap::sync);
    }
}
