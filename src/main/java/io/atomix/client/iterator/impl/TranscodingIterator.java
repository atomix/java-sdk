package io.atomix.client.iterator.impl;

import io.atomix.client.iterator.AsyncIterator;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Transcoding iterator.
 */
public class TranscodingIterator<T1, T2> implements AsyncIterator<T1> {
    private final AsyncIterator<T2> backingIterator;
    private final Function<T2, T1> elementDecoder;

    public TranscodingIterator(AsyncIterator<T2> backingIterator, Function<T2, T1> elementDecoder) {
        this.backingIterator = backingIterator;
        this.elementDecoder = elementDecoder;
    }

    @Override
    public CompletableFuture<Boolean> hasNext() {
        return backingIterator.hasNext();
    }

    @Override
    public CompletableFuture<T1> next() {
        return backingIterator.next().thenApply(elementDecoder);
    }

    @Override
    public CompletableFuture<Void> close() {
        return backingIterator.close();
    }
}