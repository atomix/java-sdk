// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set;

import io.atomix.client.PrimitiveBuilder;
import io.atomix.client.serializer.Serializer;
import io.grpc.Channel;

/**
 * Builder for DistributedSet.
 */
public abstract class DistributedSetBuilder<E> extends PrimitiveBuilder<DistributedSetBuilder<E>, DistributedSet<E>> {
    protected Serializer<E> serializer;

    protected DistributedSetBuilder(String primitiveName, Channel channel) {
        super(primitiveName, channel);
    }

    public DistributedSetBuilder<E> withSerializer(Serializer<E> serializer) {
        this.serializer = serializer;
        return this;
    }
}
