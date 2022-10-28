// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.election.impl;

import io.atomix.api.runtime.election.v1.AnointRequest;
import io.atomix.api.runtime.election.v1.CloseRequest;
import io.atomix.api.runtime.election.v1.CreateRequest;
import io.atomix.api.runtime.election.v1.DemoteRequest;
import io.atomix.api.runtime.election.v1.EnterRequest;
import io.atomix.api.runtime.election.v1.EvictRequest;
import io.atomix.api.runtime.election.v1.GetTermRequest;
import io.atomix.api.runtime.election.v1.LeaderElectionGrpc;
import io.atomix.api.runtime.election.v1.PromoteRequest;
import io.atomix.api.runtime.election.v1.Term;
import io.atomix.api.runtime.election.v1.WatchRequest;
import io.atomix.api.runtime.election.v1.WithdrawRequest;
import io.atomix.client.Cancellable;
import io.atomix.client.election.AsyncLeaderElection;
import io.atomix.client.election.LeaderElection;
import io.atomix.client.election.Leadership;
import io.atomix.client.election.LeadershipEvent;
import io.atomix.client.election.LeadershipEventListener;
import io.atomix.client.impl.AbstractAsyncPrimitive;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Leader election implementation.
 */
public class DefaultAsyncLeaderElection
    extends AbstractAsyncPrimitive<AsyncLeaderElection<String>, LeaderElection<String>, LeaderElectionGrpc.LeaderElectionStub>
    implements AsyncLeaderElection<String> {

    public DefaultAsyncLeaderElection(String name, LeaderElectionGrpc.LeaderElectionStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    protected CompletableFuture<AsyncLeaderElection<String>> create(Set<String> tags) {
        return retry(LeaderElectionGrpc.LeaderElectionStub::create, CreateRequest.newBuilder()
            .setId(id())
            .addAllTags(tags)
            .build())
            .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return retry(LeaderElectionGrpc.LeaderElectionStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Leadership<String>> enter(String identifier) {
        return retry(LeaderElectionGrpc.LeaderElectionStub::enter, EnterRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> toLeadership(response.getTerm()));
    }

    @Override
    public CompletableFuture<Void> withdraw(String identifier) {
        return retry(LeaderElectionGrpc.LeaderElectionStub::withdraw, WithdrawRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> anoint(String identifier) {
        return retry(LeaderElectionGrpc.LeaderElectionStub::anoint, AnointRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true);
    }

    @Override
    public CompletableFuture<Boolean> promote(String identifier) {
        return retry(LeaderElectionGrpc.LeaderElectionStub::promote, PromoteRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getTerm().getCandidatesList().contains(identifier));
    }

    @Override
    public CompletableFuture<Boolean> demote(String identifier) {
        return retry(LeaderElectionGrpc.LeaderElectionStub::demote, DemoteRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getTerm().getCandidatesList().contains(identifier));
    }

    @Override
    public CompletableFuture<Void> evict(String identifier) {
        return retry(LeaderElectionGrpc.LeaderElectionStub::evict, EvictRequest.newBuilder()
            .setId(id())
            .setCandidate(identifier)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Leadership<String>> getLeadership() {
        return retry(LeaderElectionGrpc.LeaderElectionStub::getTerm, GetTermRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> toLeadership(response.getTerm()));
    }

    @Override
    public CompletableFuture<Cancellable> listen(LeadershipEventListener<String> listener, Executor executor) {
        AtomicReference<Leadership<String>> leadershipRef = new AtomicReference<>();
        return retry(LeaderElectionGrpc.LeaderElectionStub::watch, WatchRequest.newBuilder()
            .setId(id())
            .build(), response -> {
            Leadership<String> leadership = leadershipRef.get();
            if (leadership == null
                || (leadership.leader() != null && leadership.term() < response.getTerm().getTerm())
                || (leadership.term() == response.getTerm().getTerm()
                && leadership.leader() == null && !response.getTerm().getLeader().isEmpty())) {
                Leadership<String> newLeadership = toLeadership(response.getTerm());
                leadershipRef.set(newLeadership);
                listener.event(new LeadershipEvent<>(LeadershipEvent.Type.CHANGE, newLeadership, leadership));
            }
        }, executor);
    }

    private static Leadership<String> toLeadership(Term term) {
        return new Leadership<>(
            term.getTerm(),
            !term.getLeader().isEmpty() ? term.getLeader() : null,
            term.getCandidatesList());
    }

    @Override
    public LeaderElection<String> sync(Duration operationTimeout) {
        return new BlockingLeaderElection<>(this, operationTimeout);
    }
}
