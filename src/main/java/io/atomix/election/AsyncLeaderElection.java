package io.atomix.election;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.AsyncPrimitive;
import io.atomix.Cancellable;
import io.atomix.election.impl.BlockingLeaderElection;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface AsyncLeaderElection<T> extends AsyncPrimitive<AsyncLeaderElection<T>, LeaderElection<T>> {

    /**
     * Attempts to become leader.
     *
     * @param identifier candidate identifier
     * @return current Leadership state
     */
    CompletableFuture<Leadership<T>> enter(T identifier);

    /**
     * Withdraws from leadership race.
     *
     * @param identifier identifier of the node to withdraw
     */
    CompletableFuture<Void> withdraw(T identifier);

    /**
     * Attempts to promote a node to leadership displacing the current leader.
     *
     * @param identifier identifier of the new leader
     * @return {@code true} if leadership transfer was successfully executed; {@code false} if it failed.
     * This operation can return {@code false} if the node to be made new leader is not registered to
     * run for election.
     */
    CompletableFuture<Boolean> anoint(T identifier);

    /**
     * Attempts to promote a node, moving it up one level in the candidate queue.
     *
     * @param identifier identifier of the candidate to promote
     * @return {@code true} if the node was promoted. This operation can fail (i.e. return
     * {@code false}) if the node is not registered to run for election.
     */
    CompletableFuture<Boolean> promote(T identifier);

    /**
     * Attempts to demote a node, moving it down one level in the candidate queue.
     *
     * @param identifier identifier of the candidate to demote
     * @return {@code true} if node was demoted. This operation can fail (i.e. return
     * {@code false}) if the node is not registered to run for election.
     */
    CompletableFuture<Boolean> demote(T identifier);

    /**
     * Attempts to evict a node from all leadership elections it is registered for.
     * <p>
     * If the node the current leader, this call will force the next candidate (if one exists)
     * to be promoted to leadership.
     *
     * @param identifier identifier
     */
    CompletableFuture<Void> evict(T identifier);

    /**
     * Returns the {@link Leadership} for the specified topic.
     *
     * @return current Leadership state
     */
    CompletableFuture<Leadership<T>> getLeadership();

    /**
     * Registers a listener to be notified of Leadership changes for all topics.
     *
     * @param listener listener to add
     * @return a cancellable context
     */
    default CompletableFuture<Cancellable> listen(LeadershipEventListener<T> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers a listener to be notified of Leadership changes for all topics.
     *
     * @param listener listener to add
     * @param executor an executor with which to call the listener
     * @return a cancellable context
     */
    CompletableFuture<Cancellable> listen(LeadershipEventListener<T> listener, Executor executor);

    @Override
    default LeaderElection<T> sync(Duration operationTimeout) {
        return new BlockingLeaderElection<>(this, operationTimeout);
    }
}
