// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter.impl;

import io.atomix.client.counter.*;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;

/**
 * Distributed counter proxy builder.
 */
public class DefaultDistributedCounterBuilder extends DistributedCounterBuilder {

    public DefaultDistributedCounterBuilder(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<DistributedCounter> buildAsync() {
        return new DefaultAsyncDistributedCounter(name(), channel())
                .create(tags())
                .thenApply(AsyncDistributedCounter::sync);
    }
}
