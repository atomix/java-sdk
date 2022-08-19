// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.value.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedSet;
import io.atomix.client.set.DistributedSetBuilder;
import io.atomix.client.set.impl.DefaultAsyncDistributedSet;
import io.atomix.client.set.impl.TranscodingAsyncDistributedSet;
import io.atomix.client.value.AsyncAtomicValue;
import io.atomix.client.value.AtomicValue;
import io.atomix.client.value.AtomicValueBuilder;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;

/**
 * Distributed set builder.
 */
public class DefaultAtomicValueBuilder<E> extends AtomicValueBuilder<E> {
    public DefaultAtomicValueBuilder(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicValue<E>> buildAsync() {
        return new DefaultAsyncAtomicValue(name(), channel())
                .create(tags())
                .thenApply(set -> new TranscodingAsyncAtomicValue<E, String>(set,
                        key -> BaseEncoding.base64().encode(serializer.serialize(key)),
                        key -> serializer.deserialize(BaseEncoding.base64().decode(key))))
                .thenApply(AsyncAtomicValue::sync);
    }
}
