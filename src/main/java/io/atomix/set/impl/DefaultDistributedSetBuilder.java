// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.set.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.set.v1.CreateRequest;
import io.atomix.api.set.v1.SetGrpc;
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
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        DefaultAsyncDistributedSet rawSet = new DefaultAsyncDistributedSet(name(), this.stub, this.executorService);
        return retry(SetGrpc.SetStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> new TranscodingAsyncDistributedSet<>(rawSet, elementEncoder, elementDecoder))
                .thenApply(AsyncDistributedSet::sync);
    }
}
