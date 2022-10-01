// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set.impl;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import io.atomix.client.collection.impl.TranscodingAsyncDistributedCollection;
import io.atomix.client.set.AsyncDistributedMultiset;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedMultiset;

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
public class TranscodingAsyncDistributedMultiset<E1, E2> extends TranscodingAsyncDistributedCollection<E1, E2> implements AsyncDistributedMultiset<E1> {
    private final AsyncDistributedMultiset<E2> backingSet;
    protected final Function<E1, E2> entryEncoder;
    protected final Function<E2, E1> entryDecoder;

    public TranscodingAsyncDistributedMultiset(
        AsyncDistributedMultiset<E2> backingSet,
        Function<E1, E2> entryEncoder,
        Function<E2, E1> entryDecoder) {
        super(backingSet, entryEncoder, entryDecoder);
        this.backingSet = backingSet;
        this.entryEncoder = e -> e == null ? null : entryEncoder.apply(e);
        this.entryDecoder = e -> e == null ? null : entryDecoder.apply(e);
    }

    @Override
    public CompletableFuture<Integer> count(Object element) {
        return backingSet.count(entryEncoder.apply((E1) element));
    }

    @Override
    public CompletableFuture<Integer> add(E1 element, int occurrences) {
        return backingSet.add(entryEncoder.apply(element), occurrences);
    }

    @Override
    public CompletableFuture<Integer> remove(Object element, int occurrences) {
        return backingSet.remove(entryEncoder.apply((E1) element), occurrences);
    }

    @Override
    public CompletableFuture<Integer> setCount(E1 element, int count) {
        return backingSet.setCount(entryEncoder.apply(element), count);
    }

    @Override
    public CompletableFuture<Boolean> setCount(E1 element, int oldCount, int newCount) {
        return backingSet.setCount(entryEncoder.apply(element), oldCount, newCount);
    }

    @Override
    public AsyncDistributedSet<E1> elementSet() {
        return new TranscodingAsyncDistributedSet<>(backingSet.elementSet(), entryEncoder, entryDecoder);
    }

    @Override
    public AsyncDistributedSet<Multiset.Entry<E1>> entrySet() {
        return new TranscodingAsyncDistributedSet<>(backingSet.entrySet(),
            entry -> Multisets.immutableEntry(entryEncoder.apply(entry.getElement()), entry.getCount()),
            entry -> Multisets.immutableEntry(entryDecoder.apply(entry.getElement()), entry.getCount()));
    }

    @Override
    public DistributedMultiset<E1> sync(Duration operationTimeout) {
        return new BlockingDistributedMultiset<>(this, operationTimeout);
    }
}