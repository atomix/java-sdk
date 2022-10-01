// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.election.impl;

import com.google.common.io.BaseEncoding;
import io.atomix.api.runtime.election.v1.LeaderElectionGrpc;
import io.atomix.client.election.AsyncLeaderElection;
import io.atomix.client.election.LeaderElection;
import io.atomix.client.election.LeaderElectionBuilder;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Leader election builder.
 */
public class DefaultLeaderElectionBuilder<T> extends LeaderElectionBuilder<T> {
    public DefaultLeaderElectionBuilder(String name, Channel channel, ScheduledExecutorService executorService) {
        super(name, channel, executorService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<LeaderElection<T>> buildAsync() {
        return new DefaultAsyncLeaderElection(name(), LeaderElectionGrpc.newStub(channel()), executor())
            .create(tags())
            .thenApply(set -> new TranscodingAsyncLeaderElection<T, String>(set,
                key -> BaseEncoding.base64().encode(serializer.encode(key)),
                key -> serializer.decode(BaseEncoding.base64().decode(key))))
            .thenApply(AsyncLeaderElection::sync);
    }
}
