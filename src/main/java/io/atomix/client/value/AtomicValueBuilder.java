// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.value;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for AtomicValue.
 */
public abstract class AtomicValueBuilder<V> extends PrimitiveBuilder<AtomicValueBuilder<V>, AtomicValue<V>> {
    protected Function<V, String> encoder;
    protected Function<String, V> decoder;

    protected AtomicValueBuilder(AtomixChannel channel) {
        super(channel);
    }

    /**
     * Sets the encoder.
     *
     * @param encoder the encoder
     * @return the builder
     */
    public AtomicValueBuilder<V> withEncoder(Function<V, String> encoder) {
        this.encoder = checkNotNull(encoder, "encoder cannot be null");
        return this;
    }

    /**
     * Sets the key decoder.
     *
     * @param decoder the key decoder
     * @return the builder
     */
    public AtomicValueBuilder<V> withDecoder(Function<String, V> decoder) {
        this.decoder = checkNotNull(decoder, "decoder cannot be null");
        return this;
    }
}
