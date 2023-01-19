// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.multimap.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.multimap.v1.CreateRequest;
import io.atomix.api.multimap.v1.MultiMapGrpc;
import io.atomix.multimap.AsyncDistributedMultimap;
import io.atomix.multimap.DistributedMultimap;
import io.atomix.multimap.DistributedMultimapBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultDistributedMultimapBuilder<K, V> extends DistributedMultimapBuilder<K, V> {
    public DefaultDistributedMultimapBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<DistributedMultimap<K, V>> buildAsync() {
        if (keyEncoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("keyEncoder cannot be null"));
        }
        if (keyDecoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("keyDecoder cannot be null"));
        }
        if (valueEncoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("valueEncoder cannot be null"));
        }
        if (valueDecoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("valueDecoder cannot be null"));
        }
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        DefaultAsyncDistributedMultimap rawMultiMap = new DefaultAsyncDistributedMultimap(name(), this.stub, this.executorService);
        return retry(MultiMapGrpc.MultiMapStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> new TranscodingAsyncDistributedMultimap<>(
                        rawMultiMap, keyEncoder, keyDecoder, valueEncoder, valueDecoder))
                .thenApply(AsyncDistributedMultimap::sync);

    }
}
