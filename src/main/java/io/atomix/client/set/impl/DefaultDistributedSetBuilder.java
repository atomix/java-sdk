// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.set.v1.SetGrpc;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedSet;
import io.atomix.client.set.DistributedSetBuilder;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Distributed set builder.
 */
public class DefaultDistributedSetBuilder<E> extends DistributedSetBuilder<E> {
    public DefaultDistributedSetBuilder(String name, Channel channel, ScheduledExecutorService executorService) {
        super(name, channel, executorService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<DistributedSet<E>> buildAsync() {
        return new DefaultAsyncDistributedSet(name(), SetGrpc.newStub(channel()), executor())
            .create(tags())
            .thenApply(set -> new TranscodingAsyncDistributedSet<E, String>(set,
                key -> BaseEncoding.base64().encode(serializer.encode(key)),
                key -> serializer.decode(BaseEncoding.base64().decode(key))))
            .thenApply(AsyncDistributedSet::sync);
    }
}
