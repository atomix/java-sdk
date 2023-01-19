// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.value.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.value.v1.CreateRequest;
import io.atomix.api.value.v1.ValueGrpc;
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
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        DefaultAsyncAtomicValue rawValue = new DefaultAsyncAtomicValue(name(), this.stub, this.executorService);
        return retry(ValueGrpc.ValueStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> new TranscodingAsyncAtomicValue<>(rawValue, encoder, decoder))
                .thenApply(AsyncAtomicValue::sync);
    }
}
