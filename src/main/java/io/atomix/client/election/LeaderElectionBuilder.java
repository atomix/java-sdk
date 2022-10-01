package io.atomix.client.election;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;

/**
 * Builder for {@link LeaderElection}.
 */
public abstract class LeaderElectionBuilder<T> extends PrimitiveBuilder<LeaderElectionBuilder<T>, LeaderElection<T>> {
    protected LeaderElectionBuilder(AtomixChannel channel) {
        super(channel);
    }
}
