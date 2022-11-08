// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.countermap;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for AtomicCounter.
 */
public abstract class AtomicCounterMapBuilder<K> extends PrimitiveBuilder<AtomicCounterMapBuilder<K>, AtomicCounterMap<K>> {
    protected Function<K, String> keyEncoder;
    protected Function<String, K> keyDecoder;

    protected AtomicCounterMapBuilder(AtomixChannel channel) {
        super(channel);
    }

    /**
     * Sets the key encoder.
     *
     * @param keyEncoder the key encoder
     * @return the builder
     */
    public AtomicCounterMapBuilder<K> withKeyEncoder(Function<K, String> keyEncoder) {
        this.keyEncoder = checkNotNull(keyEncoder, "keyEncoder cannot be null");
        return this;
    }

    /**
     * Sets the key decoder.
     *
     * @param keyDecoder the key decoder
     * @return the builder
     */
    public AtomicCounterMapBuilder<K> withKeyDecoder(Function<String, K> keyDecoder) {
        this.keyDecoder = checkNotNull(keyDecoder, "keyDecoder cannot be null");
        return this;
    }
}
