// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.election.impl;

import io.atomix.Cancellable;
import io.atomix.DelegatingAsyncPrimitive;
import io.atomix.election.AsyncLeaderElection;
import io.atomix.election.LeaderElection;
import io.atomix.election.Leadership;
import io.atomix.election.LeadershipEventListener;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Delegating asynchronous leader election.
 */
public class DelegatingAsyncLeaderElection<T>
        extends DelegatingAsyncPrimitive<AsyncLeaderElection<T>, LeaderElection<T>, AsyncLeaderElection<T>>
        implements AsyncLeaderElection<T> {

    public DelegatingAsyncLeaderElection(AsyncLeaderElection<T> primitive) {
        super(primitive);
    }

    @Override
    public CompletableFuture<Leadership<T>> enter(T identifier) {
        return delegate().enter(identifier);
    }

    @Override
    public CompletableFuture<Void> withdraw(T identifier) {
        return delegate().withdraw(identifier);
    }

    @Override
    public CompletableFuture<Boolean> anoint(T identifier) {
        return delegate().anoint(identifier);
    }

    @Override
    public CompletableFuture<Void> evict(T identifier) {
        return delegate().evict(identifier);
    }

    @Override
    public CompletableFuture<Boolean> promote(T identifier) {
        return delegate().promote(identifier);
    }

    @Override
    public CompletableFuture<Boolean> demote(T identifier) {
        return delegate().demote(identifier);
    }

    @Override
    public CompletableFuture<Leadership<T>> getLeadership() {
        return delegate().getLeadership();
    }

    @Override
    public CompletableFuture<Cancellable> listen(LeadershipEventListener<T> listener, Executor executor) {
        return delegate().listen(listener, executor);
    }

    @Override
    public LeaderElection<T> sync(Duration operationTimeout) {
        return new BlockingLeaderElection<>(this, operationTimeout);
    }
}
