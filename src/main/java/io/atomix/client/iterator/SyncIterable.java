package io.atomix.client.iterator;

/**
 * Synchronous iterable primitive.
 */
public interface SyncIterable<T> extends Iterable<T> {

    /**
     * Returns the synchronous iterator.
     *
     * @return the synchronous iterator
     */
    SyncIterator<T> iterator();

}