package io.atomix.client.collection;

import io.atomix.client.event.EventListener;

/**
 * Listener to be notified about updates to a DistributedSet.
 */
public interface CollectionEventListener<E> extends EventListener<CollectionEvent<E>> {
}