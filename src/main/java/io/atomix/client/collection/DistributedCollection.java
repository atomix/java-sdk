package io.atomix.client.collection;

import io.atomix.client.SyncPrimitive;
import io.atomix.client.iterator.SyncIterable;
import io.atomix.client.iterator.SyncIterator;

import java.util.Collection;

/**
 * Distributed collection.
 */
public interface DistributedCollection<E> extends SyncPrimitive, SyncIterable<E>, Collection<E> {
    @Override
    SyncIterator<E> iterator();

    /**
     * Registers the specified listener to be notified whenever
     * the collection is updated.
     *
     * @param listener listener to notify about collection update events
     */
    void addListener(CollectionEventListener<E> listener);

    /**
     * Unregisters the specified listener.
     *
     * @param listener listener to unregister.
     */
    void removeListener(CollectionEventListener<E> listener);

    @Override
    AsyncDistributedCollection<E> async();

}