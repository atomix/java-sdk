// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.counter.impl;

import io.atomix.api.runtime.counter.v1.CounterGrpc;
import io.atomix.AtomixChannel;
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
        return new DefaultAsyncAtomicCounter(name(), CounterGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(AsyncAtomicCounter::sync);
    }
}
