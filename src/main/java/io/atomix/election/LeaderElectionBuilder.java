package io.atomix.election;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for {@link LeaderElection}.
 */
public abstract class LeaderElectionBuilder<T> extends PrimitiveBuilder<LeaderElectionBuilder<T>, LeaderElection<T>> {
    protected Function<T, String> encoder;
    protected Function<String, T> decoder;

    protected LeaderElectionBuilder(AtomixChannel channel) {
        super(channel);
    }

    /**
     * Sets the encoder.
     *
     * @param encoder the encoder
     * @return the builder
     */
    public LeaderElectionBuilder<T> withEncoder(Function<T, String> encoder) {
        this.encoder = checkNotNull(encoder, "encoder cannot be null");
        return this;
    }

    /**
     * Sets the key decoder.
     *
     * @param decoder the key decoder
     * @return the builder
     */
    public LeaderElectionBuilder<T> withDecoder(Function<String, T> decoder) {
        this.decoder = checkNotNull(decoder, "decoder cannot be null");
        return this;
    }
}
