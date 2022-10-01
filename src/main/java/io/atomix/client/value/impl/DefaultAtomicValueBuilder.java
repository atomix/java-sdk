// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.value.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.value.v1.ValueGrpc;
import io.atomix.client.AtomixChannel;
import io.atomix.client.value.AsyncAtomicValue;
import io.atomix.client.value.AtomicValue;
import io.atomix.client.value.AtomicValueBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Distributed set builder.
 */
public class DefaultAtomicValueBuilder<E> extends AtomicValueBuilder<E> {
    public DefaultAtomicValueBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicValue<E>> buildAsync() {
        return new DefaultAsyncAtomicValue(name(), ValueGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(set -> new TranscodingAsyncAtomicValue<E, String>(set,
                key -> BaseEncoding.base64().encode(serializer.encode(key)),
                key -> serializer.decode(BaseEncoding.base64().decode(key))))
            .thenApply(AsyncAtomicValue::sync);
    }
}
