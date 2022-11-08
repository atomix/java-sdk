// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.multimap;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;
import io.atomix.client.map.AtomicMapBuilder;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for AtomicCounter.
 */
public abstract class DistributedMultimapBuilder<K, V> extends PrimitiveBuilder<DistributedMultimapBuilder<K, V>, DistributedMultimap<K, V>> {
    protected Function<K, String> keyEncoder;
    protected Function<String, K> keyDecoder;
    protected Function<V, String> valueEncoder;
    protected Function<String, V> valueDecoder;

    protected DistributedMultimapBuilder(AtomixChannel channel) {
        super(channel);
    }

    /**
     * Sets the key encoder.
     *
     * @param keyEncoder the key encoder
     * @return the builder
     */
    public DistributedMultimapBuilder<K, V> withKeyEncoder(Function<K, String> keyEncoder) {
        this.keyEncoder = checkNotNull(keyEncoder, "keyEncoder cannot be null");
        return this;
    }

    /**
     * Sets the key decoder.
     *
     * @param keyDecoder the key decoder
     * @return the builder
     */
    public DistributedMultimapBuilder<K, V> withKeyDecoder(Function<String, K> keyDecoder) {
        this.keyDecoder = checkNotNull(keyDecoder, "keyDecoder cannot be null");
        return this;
    }

    /**
     * Sets the value encoder.
     *
     * @param valueEncoder the value encoder
     * @return the builder
     */
    public DistributedMultimapBuilder<K, V> withValueEncoder(Function<V, String> valueEncoder) {
        this.valueEncoder = checkNotNull(valueEncoder, "valueEncoder cannot be null");
        return this;
    }

    /**
     * Sets the value decoder.
     *
     * @param valueDecoder the value decoder
     * @return the builder
     */
    public DistributedMultimapBuilder<K, V> withValueDecoder(Function<String, V> valueDecoder) {
        this.valueDecoder = checkNotNull(valueDecoder, "valueDecoder cannot be null");
        return this;
    }
}
