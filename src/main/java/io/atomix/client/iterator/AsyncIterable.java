package io.atomix.client.iterator;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Asynchronously iterable object.
 */
public interface AsyncIterable<T> {

    /**
     * Returns an asynchronous iterator.
     *
     * @return an asynchronous iterator
     */
    AsyncIterator<T> iterator();

    /**
     * Returns a stream.
     *
     * @return a new stream
     */
    default Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator().sync(), Spliterator.ORDERED), false);
    }
}