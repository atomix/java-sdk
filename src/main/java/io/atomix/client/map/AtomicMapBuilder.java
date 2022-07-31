// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map;

import io.atomix.client.PrimitiveBuilder;
import io.atomix.client.serializer.Serializer;
import io.grpc.Channel;

/**
 * Builder for AtomicCounter.
 */
public abstract class AtomicMapBuilder<K, V> extends PrimitiveBuilder<AtomicMapBuilder<K, V>, AtomicMap<K, V>> {
    protected Serializer<K> keySerializer;
    protected Serializer<V> valueSerializer;

    protected AtomicMapBuilder(String primitiveName, Channel channel) {
        super(primitiveName, channel);
    }

    public AtomicMapBuilder<K, V> withKeySerializer(Serializer<K> serializer) {
        this.keySerializer = serializer;
        return this;
    }

    public AtomicMapBuilder<K, V> withValueSerializer(Serializer<V> serializer) {
        this.valueSerializer = serializer;
        return this;
    }
}
