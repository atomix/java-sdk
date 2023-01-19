// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.counter.impl;

import io.atomix.api.counter.v1.CounterGrpc;
import io.atomix.AtomixChannel;
import io.atomix.api.counter.v1.CreateRequest;
import io.atomix.counter.AsyncAtomicCounter;
import io.atomix.counter.AtomicCounter;
import io.atomix.counter.AtomicCounterBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicCounterBuilder extends AtomicCounterBuilder {

    public DefaultAtomicCounterBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicCounter> buildAsync() {
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        return retry(CounterGrpc.CounterStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> new DefaultAsyncAtomicCounter(name(), this.stub, this.executorService))
                .thenApply(AsyncAtomicCounter::sync);
    }
}
