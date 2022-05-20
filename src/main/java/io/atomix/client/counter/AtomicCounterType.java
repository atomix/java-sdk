// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import io.atomix.client.PrimitiveType;
import io.atomix.client.counter.impl.DefaultAtomicCounterBuilder;
import io.atomix.client.utils.ThreadContext;
import io.grpc.Channel;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Atomic counter primitive type.
 */
public class AtomicCounterType implements PrimitiveType<AtomicCounterBuilder, AtomicCounter> {
    private static final AtomicCounterType INSTANCE = new AtomicCounterType();

    /**
     * Returns a new atomic counter type.
     *
     * @return a new atomic counter type
     */
    public static AtomicCounterType instance() {
        return INSTANCE;
    }

    @Override
    public AtomicCounterBuilder newBuilder(String primitiveName, Channel serviceChannel,
                                           ThreadContext threadContext) {
        return new DefaultAtomicCounterBuilder(primitiveName, serviceChannel, threadContext);
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }
}
