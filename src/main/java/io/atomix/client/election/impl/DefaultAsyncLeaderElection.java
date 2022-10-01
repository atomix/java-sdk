// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.election.impl;

import io.atomix.api.runtime.election.v1.AnointRequest;
import io.atomix.api.runtime.election.v1.CloseRequest;
import io.atomix.api.runtime.election.v1.CreateRequest;
import io.atomix.api.runtime.election.v1.EnterRequest;
import io.atomix.api.runtime.election.v1.EvictRequest;
import io.atomix.api.runtime.election.v1.GetTermRequest;
import io.atomix.api.runtime.election.v1.LeaderElectionGrpc;
import io.atomix.api.runtime.election.v1.PromoteRequest;
import io.atomix.api.runtime.election.v1.WatchRequest;
import io.atomix.api.runtime.election.v1.WithdrawRequest;
import io.atomix.client.Cancellable;
import io.atomix.client.election.AsyncLeaderElection;
import io.atomix.client.election.Leader;
import io.atomix.client.election.LeaderElection;
import io.atomix.client.election.Leadership;
import io.atomix.client.election.LeadershipEvent;
import io.atomix.client.election.LeadershipEventListener;
import io.atomix.client.impl.AbstractAsyncPrimitive;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Leader election implementation.
 */
public class DefaultAsyncLeaderElection
    extends AbstractAsyncPrimitive<LeaderElectionGrpc.LeaderElectionStub, AsyncLeaderElection<String>>
    implements AsyncLeaderElection<String> {

    public DefaultAsyncLeaderElection(String name, LeaderElectionGrpc.LeaderElectionStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    protected CompletableFuture<AsyncLeaderElection<String>> create(Map<String, String> tags) {
        return execute(LeaderElectionGrpc.LeaderElectionStub::create, CreateRequest.newBuilder()
            .setId(id())
            .putAllTags(tags)
            .build())
            .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return execute(LeaderElectionGrpc.LeaderElectionStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Leadership<String>> enter(String identifier) {
        return execute(LeaderElectionGrpc.LeaderElectionStub::enter, EnterRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Leadership<>(
                !response.getTerm().getLeader().isEmpty() ? null :
                    new Leader<>(response.getTerm().getLeader(), response.getTerm().getTerm(), System.currentTimeMillis()),
                response.getTerm().getCandidatesList()));
    }

    @Override
    public CompletableFuture<Void> withdraw(String identifier) {
        return execute(LeaderElectionGrpc.LeaderElectionStub::withdraw, WithdrawRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> anoint(String identifier) {
        return execute(LeaderElectionGrpc.LeaderElectionStub::anoint, AnointRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true);
    }

    @Override
    public CompletableFuture<Boolean> promote(String identifier) {
        return execute(LeaderElectionGrpc.LeaderElectionStub::promote, PromoteRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true);
    }

    @Override
    public CompletableFuture<Void> evict(String identifier) {
        return execute(LeaderElectionGrpc.LeaderElectionStub::evict, EvictRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Leadership<String>> getLeadership() {
        return execute(LeaderElectionGrpc.LeaderElectionStub::getTerm, GetTermRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Leadership<>(
                !response.getTerm().getLeader().isEmpty() ? null :
                    new Leader<>(response.getTerm().getLeader(), response.getTerm().getTerm(), System.currentTimeMillis()),
                response.getTerm().getCandidatesList()));
    }

    @Override
    public CompletableFuture<Cancellable> listen(LeadershipEventListener<String> listener, Executor executor) {
        return execute(LeaderElectionGrpc.LeaderElectionStub::watch, WatchRequest.newBuilder()
            .setId(id())
            .build(), response -> listener.event(new LeadershipEvent<>(LeadershipEvent.Type.CHANGE,
            new Leadership<>(
                !response.getTerm().getLeader().isEmpty() ? null :
                    new Leader<>(response.getTerm().getLeader(), response.getTerm().getTerm(), System.currentTimeMillis()),
                response.getTerm().getCandidatesList()), null)), executor);
    }

    @Override
    public LeaderElection<String> sync(Duration operationTimeout) {
        return new BlockingLeaderElection<>(this, operationTimeout.toMillis());
    }
}
