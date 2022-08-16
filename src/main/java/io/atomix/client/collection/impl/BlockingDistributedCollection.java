// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.collection.impl;

import com.google.common.base.Throwables;
import io.atomix.client.Cancellable;
import io.atomix.client.PrimitiveException;
import io.atomix.client.Synchronous;
import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.iterator.SyncIterator;
import io.atomix.client.iterator.impl.BlockingIterator;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

/**
 * Implementation of {@link DistributedCollection} that merely delegates to a {@link AsyncDistributedCollection} and
 * waits for the operation to complete.
 *
 * @param <E> collection element type
 */
public class BlockingDistributedCollection<E> extends Synchronous<AsyncDistributedCollection<E>> implements DistributedCollection<E> {

    private final long operationTimeoutMillis;

    private final AsyncDistributedCollection<E> asyncCollection;

    public BlockingDistributedCollection(AsyncDistributedCollection<E> asyncCollection, long operationTimeoutMillis) {
        super(asyncCollection);
        this.asyncCollection = asyncCollection;
        this.operationTimeoutMillis = operationTimeoutMillis;
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
        return new BlockingIterator<>(asyncCollection.iterator(), operationTimeoutMillis);
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

    protected <T> T complete(CompletableFuture<T> future) {
        try {
            return future.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrimitiveException.Interrupted();
        } catch (TimeoutException e) {
            throw new PrimitiveException.Timeout();
        } catch (ExecutionException e) {
            Throwable cause = Throwables.getRootCause(e);
            if (cause instanceof PrimitiveException) {
                throw (PrimitiveException) cause;
            } else if (cause instanceof NoSuchElementException) {
                throw (NoSuchElementException) cause;
            } else {
                throw new PrimitiveException(cause);
            }
        }
    }
}