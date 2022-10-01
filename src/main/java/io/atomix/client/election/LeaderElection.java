package io.atomix.client.election;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.AtomixChannel;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.election.impl.DefaultLeaderElectionBuilder;

import java.util.concurrent.Executor;

/**
 * {@code LeaderElector} provides the same functionality as {@link AsyncLeaderElection} with
 * the only difference that all its methods block until the corresponding operation completes.
 */
public interface LeaderElection<T> extends SyncPrimitive<LeaderElection<T>, AsyncLeaderElection<T>> {

    /**
     * Returns a new LeaderElection builder.
     *
     * @param channel the AtomixChannel
     * @return the LeaderElection builder
     */
    static <T> LeaderElectionBuilder<T> builder(AtomixChannel channel) {
        return new DefaultLeaderElectionBuilder<>(channel);
    }

    /**
     * Attempts to become leader.
     *
     * @param identifier candidate identifier
     * @return current Leadership state
     */
    Leadership<T> enter(T identifier);

    /**
     * Withdraws from leadership race.
     *
     * @param identifier identifier of the node to withdraw
     */
    void withdraw(T identifier);

    /**
     * Attempts to promote a node to leadership displacing the current leader.
     *
     * @param identifier identifier of the new leader
     * @return {@code true} if leadership transfer was successfully executed; {@code false} if it failed.
     * This operation can return {@code false} if the node to be made new leader is not registered to
     * run for election.
     */
    boolean anoint(T identifier);

    /**
     * Attempts to promote a node to top of candidate list.
     *
     * @param identifier identifier of the new top candidate
     * @return {@code true} if node is now the top candidate. This operation can fail (i.e. return
     * {@code false}) if the node is not registered to run for election.
     */
    boolean promote(T identifier);

    /**
     * Attempts to evict a node from all leadership elections it is registered for.
     * <p>
     * If the node the current leader, this call will force the next candidate (if one exists)
     * to be promoted to leadership.
     *
     * @param identifier identifier
     */
    void evict(T identifier);

    /**
     * Returns the {@link Leadership} for the specified topic.
     *
     * @return current Leadership state
     */
    Leadership<T> getLeadership();

    /**
     * Registers a listener to be notified of Leadership changes for all topics.
     *
     * @param listener listener to add
     * @return a cancellable context
     */
    default Cancellable listen(LeadershipEventListener<T> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers a listener to be notified of Leadership changes for all topics.
     *
     * @param listener listener to add
     * @param executor an executor with which to call the listener
     * @return a cancellable context
     */
    Cancellable listen(LeadershipEventListener<T> listener, Executor executor);
}
