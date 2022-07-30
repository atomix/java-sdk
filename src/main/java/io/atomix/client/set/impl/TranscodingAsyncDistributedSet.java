package io.atomix.client.set.impl;

import io.atomix.client.collection.impl.TranscodingAsyncDistributedCollection;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedSet;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * An {@code AsyncDistributedSet} that maps its operations to operations on a
 * differently typed {@code AsyncDistributedSet} by transcoding operation inputs and outputs.
 *
 * @param <E2> key type of other map
 * @param <E1> key type of this map
 */
public class TranscodingAsyncDistributedSet<E1, E2> extends TranscodingAsyncDistributedCollection<E1, E2> implements AsyncDistributedSet<E1> {
    private final AsyncDistributedSet<E2> backingSet;
    protected final Function<E1, E2> entryEncoder;
    protected final Function<E2, E1> entryDecoder;

    public TranscodingAsyncDistributedSet(
            AsyncDistributedSet<E2> backingSet,
            Function<E1, E2> entryEncoder,
            Function<E2, E1> entryDecoder) {
        super(backingSet, entryEncoder, entryDecoder);
        this.backingSet = backingSet;
        this.entryEncoder = e -> e == null ? null : entryEncoder.apply(e);
        this.entryDecoder = e -> e == null ? null : entryDecoder.apply(e);
    }

    @Override
    public DistributedSet<E1> sync(Duration operationTimeout) {
        return new BlockingDistributedSet<>(this, operationTimeout.toMillis());
    }
}