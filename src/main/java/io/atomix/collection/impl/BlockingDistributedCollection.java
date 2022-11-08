// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.collection.impl;

import io.atomix.Cancellable;
import io.atomix.Synchronous;
import io.atomix.collection.AsyncDistributedCollection;
import io.atomix.collection.CollectionEventListener;
import io.atomix.collection.DistributedCollection;
import io.atomix.iterator.SyncIterator;
import io.atomix.iterator.impl.BlockingIterator;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link DistributedCollection} that merely delegates to a {@link AsyncDistributedCollection} and
 * waits for the operation to complete.
 *
 * @param <E> collection element type
 */
public class BlockingDistributedCollection<E> extends Synchronous<DistributedCollection<E>, AsyncDistributedCollection<E>> implements DistributedCollection<E> {

    private final AsyncDistributedCollection<E> asyncCollection;

    public BlockingDistributedCollection(AsyncDistributedCollection<E> asyncCollection, Duration operationTimeout) {
        super(asyncCollection, operationTimeout);
        this.asyncCollection = asyncCollection;
    }

    @Override
    public int size() {
        return complete(asyncCollection.size());
    }

    @Override
    public boolean isEmpty() {
        return complete(asyncCollection.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return complete(asyncCollection.contains((E) o));
    }

    @Override
    public boolean add(E e) {
        return complete(asyncCollection.add(e));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        return complete(asyncCollection.remove((E) o));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsAll(Collection<?> c) {
        return complete(asyncCollection.containsAll((Collection<? extends E>) c));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return complete(asyncCollection.addAll(c));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection<?> c) {
        return complete(asyncCollection.retainAll((Collection<? extends E>) c));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection<?> c) {
        return complete(asyncCollection.removeAll((Collection<? extends E>) c));
    }

    @Override
    public void clear() {
        complete(asyncCollection.clear());
    }

    @Override
    public SyncIterator<E> iterator() {
        return new BlockingIterator<>(asyncCollection.iterator(), operationTimeout);
    }

    @Override
    public Cancellable listen(CollectionEventListener<E> listener, Executor executor) {
        return complete(asyncCollection.listen(listener, executor));
    }

    @Override
    public Object[] toArray() {
        return stream().toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        Object[] copy = toArray();
        System.arraycopy(copy, 0, array, 0, Math.min(copy.length, array.length));
        return array;
    }

    @Override
    public AsyncDistributedCollection<E> async() {
        return asyncCollection;
    }
}