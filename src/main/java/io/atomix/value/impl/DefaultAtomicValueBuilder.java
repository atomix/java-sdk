// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.value.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.runtime.value.v1.ValueGrpc;
import io.atomix.value.AsyncAtomicValue;
import io.atomix.value.AtomicValue;
import io.atomix.value.AtomicValueBuilder;

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
        if (encoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("encoder cannot be null"));
        }
        if (decoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("decoder cannot be null"));
        }
        return new DefaultAsyncAtomicValue(name(), ValueGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(set -> new TranscodingAsyncAtomicValue<>(set, encoder, decoder))
            .thenApply(AsyncAtomicValue::sync);
    }
}
