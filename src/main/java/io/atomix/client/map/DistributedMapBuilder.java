// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map;

import io.atomix.client.PrimitiveBuilder;
import io.atomix.client.serializer.Serializer;
import io.grpc.Channel;

/**
 * Builder for {@link DistributedMap} instances.
 *
 * @param <K> type for map key
 * @param <V> type for map value
 */
public abstract class DistributedMapBuilder<K, V> extends PrimitiveBuilder<DistributedMapBuilder<K, V>, DistributedMap<K, V>> {
    protected Serializer<K> keySerializer;
    protected Serializer<V> valueSerializer;

    protected DistributedMapBuilder(String primitiveName, Channel channel) {
        super(primitiveName, channel);
    }

    public DistributedMapBuilder<K, V> withKeySerializer(Serializer<K> serializer) {
        this.keySerializer = serializer;
        return this;
    }

    public DistributedMapBuilder<K, V> withValueSerializer(Serializer<V> serializer) {
        this.valueSerializer = serializer;
        return this;
    }
}
