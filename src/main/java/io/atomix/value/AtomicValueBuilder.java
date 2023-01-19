// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.value;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;
import io.atomix.api.value.v1.ValueGrpc;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for AtomicValue.
 */
public abstract class AtomicValueBuilder<V>
        extends PrimitiveBuilder<AtomicValueBuilder<V>, AtomicValue<V>, ValueGrpc.ValueStub> {
    protected Function<V, String> encoder;
    protected Function<String, V> decoder;

    protected AtomicValueBuilder(AtomixChannel channel) {
        super(channel, ValueGrpc.newStub(channel), channel.executor());
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
