package io.atomix.client.election.impl;

import com.google.common.base.Throwables;
import io.atomix.client.Cancellable;
import io.atomix.client.PrimitiveException;
import io.atomix.client.Synchronous;
import io.atomix.client.election.AsyncLeaderElection;
import io.atomix.client.election.LeaderElection;
import io.atomix.client.election.Leadership;
import io.atomix.client.election.LeadershipEventListener;

import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default implementation for a {@code LeaderElection} backed by a {@link AsyncLeaderElection}.
 *
 * @param <V> value type
 */
public class BlockingLeaderElection<V> extends Synchronous<AsyncLeaderElection<V>> implements LeaderElection<V> {
    private final AsyncLeaderElection<V> asyncLeaderElection;
    private final long operationTimeoutMillis;

    public BlockingLeaderElection(AsyncLeaderElection<V> asyncLeaderElection, long operationTimeoutMillis) {
        super(asyncLeaderElection);
        this.asyncLeaderElection = asyncLeaderElection;
        this.operationTimeoutMillis = operationTimeoutMillis;
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

    protected <T> T complete(CompletableFuture<T> future) {
        try {
            return future.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrimitiveException.Interrupted();
        } catch (TimeoutException e) {
            throw new PrimitiveException.Timeout();
        } catch (ExecutionException e) {
            Throwable cause = Throwables.getRootCause(e);
            if (cause instanceof PrimitiveException) {
                throw (PrimitiveException) cause;
            } else if (cause instanceof ConcurrentModificationException) {
                throw (ConcurrentModificationException) cause;
            } else {
                throw new PrimitiveException(cause);
            }
        }
    }
}
