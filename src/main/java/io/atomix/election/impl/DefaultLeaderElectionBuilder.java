// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.election.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.runtime.election.v1.LeaderElectionGrpc;
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
        return new DefaultAsyncLeaderElection(name(), LeaderElectionGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(election -> new TranscodingAsyncLeaderElection<>(election, encoder, decoder))
            .thenApply(AsyncLeaderElection::sync);
    }
}
