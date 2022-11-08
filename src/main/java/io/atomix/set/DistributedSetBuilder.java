// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.set;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for DistributedSet.
 */
public abstract class DistributedSetBuilder<E> extends PrimitiveBuilder<DistributedSetBuilder<E>, DistributedSet<E>> {
    protected Function<E, String> elementEncoder;
    protected Function<String, E> elementDecoder;

    protected DistributedSetBuilder(AtomixChannel channel) {
        super(channel);
    }

    /**
     * Sets the element encoder.
     *
     * @param elementEncoder the element encoder
     * @return the builder
     */
    public DistributedSetBuilder<E> withElementEncoder(Function<E, String> elementEncoder) {
        this.elementEncoder = checkNotNull(elementEncoder, "elementEncoder cannot be null");
        return this;
    }

    /**
     * Sets the element decoder.
     *
     * @param elementDecoder the element decoder
     * @return the builder
     */
    public DistributedSetBuilder<E> withElementDecoder(Function<String, E> elementDecoder) {
        this.elementDecoder = checkNotNull(elementDecoder, "elementDecoder cannot be null");
        return this;
    }
}
