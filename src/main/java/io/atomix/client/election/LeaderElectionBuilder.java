package io.atomix.client.election;

import io.atomix.client.PrimitiveBuilder;
import io.grpc.Channel;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Builder for {@link LeaderElection}.
 */
public abstract class LeaderElectionBuilder<T> extends PrimitiveBuilder<LeaderElectionBuilder<T>, LeaderElection<T>> {
    protected LeaderElectionBuilder(String primitiveName, Channel channel, ScheduledExecutorService executorService) {
        super(primitiveName, channel, executorService);
    }
}
