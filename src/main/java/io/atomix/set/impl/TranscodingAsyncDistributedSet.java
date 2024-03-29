// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.set.impl;

import io.atomix.collection.impl.TranscodingAsyncDistributedCollection;
import io.atomix.set.AsyncDistributedSet;
import io.atomix.set.DistributedSet;

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
    public CompletableFuture<Boolean> add(E1 element, Duration ttl) {
        return backingSet.add(entryEncoder.apply(element), ttl);
    }

    @Override
    public DistributedSet<E1> sync(Duration operationTimeout) {
        return new BlockingDistributedSet<>(this, operationTimeout);
    }
}