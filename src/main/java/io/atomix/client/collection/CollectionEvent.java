package io.atomix.client.collection;

import io.atomix.client.event.AbstractEvent;

/**
 * Representation of a DistributedCollection update notification.
 *
 * @param <E> collection element type
 */
public final class CollectionEvent<E> extends AbstractEvent<CollectionEvent.Type, E> {

    /**
     * Collection event type.
     */
    public enum Type {
        /**
         * Entry added to the set.
         */
        ADD,

        /**
         * Entry removed from the set.
         */
        REMOVE,

        /**
         * Entry replayed from existing set.
         */
        REPLAY
    }

    /**
     * Creates a new event object.
     *
     * @param type  type of the event
     * @param entry entry the event concerns
     */
    public CollectionEvent(Type type, E entry) {
        super(type, entry);
    }

    /**
     * Returns the entry this event concerns.
     *
     * @return the entry
     */
    public E element() {
        return subject();
    }
}