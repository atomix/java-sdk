// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for AtomicCounter.
 */
public abstract class AtomicMapBuilder<K, V> extends PrimitiveBuilder<AtomicMapBuilder<K, V>, AtomicMap<K, V>> {
    protected Function<K, String> keyEncoder;
    protected Function<String, K> keyDecoder;
    protected Function<V, byte[]> valueEncoder;
    protected Function<byte[], V> valueDecoder;

    protected AtomicMapBuilder(AtomixChannel channel) {
        super(channel);
    }

    /**
     * Sets the key encoder.
     *
     * @param keyEncoder the key encoder
     * @return the builder
     */
    public AtomicMapBuilder<K, V> withKeyEncoder(Function<K, String> keyEncoder) {
        this.keyEncoder = checkNotNull(keyEncoder, "keyEncoder cannot be null");
        return this;
    }

    /**
     * Sets the key decoder.
     *
     * @param keyDecoder the key decoder
     * @return the builder
     */
    public AtomicMapBuilder<K, V> withKeyDecoder(Function<String, K> keyDecoder) {
        this.keyDecoder = checkNotNull(keyDecoder, "keyDecoder cannot be null");
        return this;
    }

    /**
     * Sets the value encoder.
     *
     * @param valueEncoder the value encoder
     * @return the builder
     */
    public AtomicMapBuilder<K, V> withValueEncoder(Function<V, byte[]> valueEncoder) {
        this.valueEncoder = checkNotNull(valueEncoder, "valueEncoder cannot be null");
        return this;
    }

    /**
     * Sets the value decoder.
     *
     * @param valueDecoder the value decoder
     * @return the builder
     */
    public AtomicMapBuilder<K, V> withValueDecoder(Function<byte[], V> valueDecoder) {
        this.valueDecoder = checkNotNull(valueDecoder, "valueDecoder cannot be null");
        return this;
    }
}
