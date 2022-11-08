package io.atomix.election.impl;

import io.atomix.Cancellable;
import io.atomix.Synchronous;
import io.atomix.election.AsyncLeaderElection;
import io.atomix.election.LeaderElection;
import io.atomix.election.Leadership;
import io.atomix.election.LeadershipEventListener;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Default implementation for a {@code LeaderElection} backed by a {@link AsyncLeaderElection}.
 *
 * @param <V> value type
 */
public class BlockingLeaderElection<V> extends Synchronous<LeaderElection<V>, AsyncLeaderElection<V>> implements LeaderElection<V> {
    private final AsyncLeaderElection<V> asyncLeaderElection;

    public BlockingLeaderElection(AsyncLeaderElection<V> asyncLeaderElection, Duration operationTimeout) {
        super(asyncLeaderElection, operationTimeout);
        this.asyncLeaderElection = asyncLeaderElection;
    }

    @Override
    public Leadership<V> enter(V identifier) {
        return complete(asyncLeaderElection.enter(identifier));
    }

    @Override
    public void withdraw(V identifier) {
        complete(asyncLeaderElection.withdraw(identifier));
    }

    @Override
    public boolean anoint(V identifier) {
        return complete(asyncLeaderElection.anoint(identifier));
    }

    @Override
    public boolean promote(V identifier) {
        return complete(asyncLeaderElection.promote(identifier));
    }

    @Override
    public boolean demote(V identifier) {
        return complete(asyncLeaderElection.demote(identifier));
    }

    @Override
    public void evict(V identifier) {
        complete(asyncLeaderElection.evict(identifier));
    }

    @Override
    public Leadership<V> getLeadership() {
        return complete(asyncLeaderElection.getLeadership());
    }

    @Override
    public Cancellable listen(LeadershipEventListener<V> listener, Executor executor) {
        return complete(asyncLeaderElection.listen(listener, executor));
    }

    @Override
    public AsyncLeaderElection<V> async() {
        return asyncLeaderElection;
    }
}
