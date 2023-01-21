// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.election.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.election.v1.CreateRequest;
import io.atomix.api.election.v1.LeaderElectionGrpc;
import io.atomix.election.AsyncLeaderElection;
import io.atomix.election.LeaderElection;
import io.atomix.election.LeaderElectionBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Leader election builder.
 */
public class DefaultLeaderElectionBuilder<T> extends LeaderElectionBuilder<T> {

    public DefaultLeaderElectionBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    public CompletableFuture<LeaderElection<T>> buildAsync() {
        if (encoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("encoder cannot be null"));
        }
        if (decoder == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("decoder cannot be null"));
        }
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        DefaultAsyncLeaderElection rawLeaderElection = new DefaultAsyncLeaderElection(name(), this.stub, this.executorService);
        return retry(LeaderElectionGrpc.LeaderElectionStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> {
                    // Underlay election which wraps the gRPC "election"
                    AsyncLeaderElection<T> leaderElection = new TranscodingAsyncLeaderElection<>(
                            rawLeaderElection, encoder, decoder);
                    // If config is enabled we further decorate with the caching wrap
//                    if (response.hasConfig() && response.getConfig().hasCache() &&
//                            response.getConfig().getCache().getEnabled()) {
                    leaderElection = new CachingAsyncLeaderElection<>(leaderElection);
//                    }
                    return leaderElection.sync();
                });
    }
}
