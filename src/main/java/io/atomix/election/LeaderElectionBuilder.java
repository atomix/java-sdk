package io.atomix.election;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;

/**
 * Builder for {@link LeaderElection}.
 */
public abstract class LeaderElectionBuilder<T> extends PrimitiveBuilder<LeaderElectionBuilder<T>, LeaderElection<T>> {
    protected LeaderElectionBuilder(AtomixChannel channel) {
        super(channel);
    }
}
