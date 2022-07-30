package io.atomix.client.iterator;

import java.util.Iterator;

/**
 * Synchronous iterator.
 */
public interface SyncIterator<T> extends Iterator<T> {

    /**
     * Closes the iterator.
     */
    void close();

    /**
     * Returns the underlying asynchronous iterator.
     *
     * @return the underlying asynchronous iterator
     */
    AsyncIterator<T> async();

}