// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.election.impl;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.Cancellable;
import io.atomix.election.AsyncLeaderElection;
import io.atomix.election.Leadership;
import io.atomix.election.LeadershipEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * {@code AsyncLeaderElection} that caches leadership on read.
 * <p>
 * The cache is automatically invalidated when updates are detected either locally or
 * remotely.
 * <p> This implementation only attempts to serve cached entries for
 * {@link AsyncLeaderElection#getLeadership()} getLeadership}.
 *
 * @param <T> identifier type
 */
public class CachingAsyncLeaderElection<T> extends DelegatingAsyncLeaderElection<T> {
    private CompletableFuture<Leadership<T>> cache;
    private LeadershipEventListener<T> electionListener;
    private final LeadershipEventListener<T> cacheUpdater;

    public CachingAsyncLeaderElection(AsyncLeaderElection<T> delegateLeaderElection) {
        super(delegateLeaderElection);
        cacheUpdater = event -> {
            cache = CompletableFuture.completedFuture(event.newLeadership());
            if (electionListener != null) {
                electionListener.event(event);
            }
        };
        super.listen(cacheUpdater, MoreExecutors.directExecutor());
    }

    @Override
    public CompletableFuture<Leadership<T>> enter(T identifier) {
        return super.enter(identifier).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Void> withdraw(T identifier) {
        return super.withdraw(identifier).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Boolean> anoint(T nodeId) {
        return super.anoint(nodeId).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Boolean> promote(T nodeId) {
        return super.promote(nodeId).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Boolean> demote(T identifier) {
        return super.demote(identifier).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Void> evict(T nodeId) {
        return super.evict(nodeId).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Leadership<T>> getLeadership() {
        // Technically speaking, it substitutes the cache loader
        if (cache == null) {
            cache = super.getLeadership();
        }
        return cache.whenComplete((r, e) -> {
            if (e != null) {
                cache = null;
            }
        });
    }

    @Override
    public CompletableFuture<Cancellable> listen(LeadershipEventListener<T> listener, Executor executor) {
        electionListener = listener;
        // On cancel removes itself. Not used atm, as we don't expose a "remove" API
        return CompletableFuture.completedFuture(() -> electionListener = null);
    }
}
