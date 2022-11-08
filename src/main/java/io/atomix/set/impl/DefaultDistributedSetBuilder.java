// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.set.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.runtime.set.v1.SetGrpc;
import io.atomix.set.AsyncDistributedSet;
import io.atomix.set.DistributedSet;
import io.atomix.set.DistributedSetBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Distributed set builder.
 */
public class DefaultDistributedSetBuilder<E> extends DistributedSetBuilder<E> {
    public DefaultDistributedSetBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<DistributedSet<E>> buildAsync() {
        if (elementEncoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("elementEncoder cannot be null"));
        }
        if (elementDecoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("elementDecoder cannot be null"));
        }
        return new DefaultAsyncDistributedSet(name(), SetGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(set -> new TranscodingAsyncDistributedSet<>((AsyncDistributedSet<String>) set, elementEncoder, elementDecoder))
            .thenApply(AsyncDistributedSet::sync);
    }
}
