// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.collection;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.AsyncPrimitive;
import io.atomix.Cancellable;
import io.atomix.collection.impl.BlockingDistributedCollection;
import io.atomix.iterator.AsyncIterable;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Asynchronous distributed collection.
 */
public interface AsyncDistributedCollection<E> extends AsyncPrimitive<AsyncDistributedCollection<E>, DistributedCollection<E>>, AsyncIterable<E> {

    /**
     * Adds the specified element to this collection if it is not already present (optional operation).
     *
     * @param element element to add
     * @return {@code true} if this collection did not already contain the specified element.
     */
    CompletableFuture<Boolean> add(E element);

    /**
     * Removes the specified element to this collection if it is present (optional operation).
     *
     * @param element element to remove
     * @return {@code true} if this collection contained the specified element
     */
    CompletableFuture<Boolean> remove(E element);

    /**
     * Returns the number of elements in the collection.
     *
     * @return size of the collection
     */
    CompletableFuture<Integer> size();

    /**
     * Returns if the collection is empty.
     *
     * @return {@code true} if this collection is empty
     */
    CompletableFuture<Boolean> isEmpty();

    /**
     * Removes all elements from the collection.
     *
     * @return CompletableFuture that is completed when the operation completes
     */
    CompletableFuture<Void> clear();

    /**
     * Returns if this collection contains the specified element.
     *
     * @param element element to check
     * @return {@code true} if this collection contains the specified element
     */
    CompletableFuture<Boolean> contains(E element);

    /**
     * Adds all of the elements in the specified collection to this collection if they're not
     * already present (optional operation).
     *
     * @param c collection containing elements to be added to this collection
     * @return {@code true} if this collection contains all elements in the collection
     */
    CompletableFuture<Boolean> addAll(Collection<? extends E> c);

    /**
     * Returns if this collection contains all the elements in specified collection.
     *
     * @param c collection
     * @return {@code true} if this collection contains all elements in the collection
     */
    CompletableFuture<Boolean> containsAll(Collection<? extends E> c);

    /**
     * Retains only the elements in this collection that are contained in the specified collection (optional operation).
     *
     * @param c collection containing elements to be retained in this collection
     * @return {@code true} if this collection changed as a result of the call
     */
    CompletableFuture<Boolean> retainAll(Collection<? extends E> c);

    /**
     * Removes from this collection all of its elements that are contained in the specified collection (optional operation).
     *
     * @param c collection containing elements to be removed from this collection
     * @return {@code true} if this collection changed as a result of the call
     */
    CompletableFuture<Boolean> removeAll(Collection<? extends E> c);

    /**
     * Registers the specified listener to be notified whenever
     * the collection is updated.
     *
     * @param listener listener to notify about collection update events
     * @return CompletableFuture that is completed when the operation completes
     */
    default CompletableFuture<Cancellable> listen(CollectionEventListener<E> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever
     * the collection is updated.
     *
     * @param listener listener to notify about collection update events
     * @param executor executor on which to call event listener
     * @return CompletableFuture that is completed when the operation completes
     */
    CompletableFuture<Cancellable> listen(CollectionEventListener<E> listener, Executor executor);

    @Override
    default DistributedCollection<E> sync(Duration operationTimeout) {
        return new BlockingDistributedCollection<>(this, operationTimeout);
    }
}
