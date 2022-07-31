package io.atomix.client.collection.impl;

import io.atomix.client.Cancellable;
import io.atomix.client.DelegatingAsyncPrimitive;
import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.collection.CollectionEvent;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.iterator.impl.TranscodingIterator;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An {@code AsyncDistributedCollection} that maps its operations to operations on a
 * differently typed {@code AsyncDistributedCollection} by transcoding operation inputs and outputs.
 *
 * @param <E2> element type of other collection
 * @param <E1> element type of this collection
 */
public class TranscodingAsyncDistributedCollection<E1, E2> extends DelegatingAsyncPrimitive implements AsyncDistributedCollection<E1> {
    private final AsyncDistributedCollection<E2> backingCollection;
    private final Function<E1, E2> elementEncoder;
    private final Function<E2, E1> elementDecoder;

    public TranscodingAsyncDistributedCollection(
            AsyncDistributedCollection<E2> backingCollection,
            Function<E1, E2> elementEncoder,
            Function<E2, E1> elementDecoder) {
        super(backingCollection);
        this.backingCollection = backingCollection;
        this.elementEncoder = k -> k == null ? null : elementEncoder.apply(k);
        this.elementDecoder = k -> k == null ? null : elementDecoder.apply(k);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return backingCollection.size();
    }

    @Override
    public CompletableFuture<Boolean> add(E1 element) {
        return backingCollection.add(elementEncoder.apply(element));
    }

    @Override
    public CompletableFuture<Boolean> remove(E1 element) {
        return backingCollection.remove(elementEncoder.apply(element));
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        return backingCollection.isEmpty();
    }

    @Override
    public CompletableFuture<Void> clear() {
        return backingCollection.clear();
    }

    @Override
    public CompletableFuture<Boolean> contains(E1 element) {
        return backingCollection.contains(elementEncoder.apply(element));
    }

    @Override
    public CompletableFuture<Boolean> addAll(Collection<? extends E1> c) {
        return backingCollection.addAll(c.stream().map(elementEncoder).collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Boolean> containsAll(Collection<? extends E1> c) {
        return backingCollection.containsAll(c.stream().map(elementEncoder).collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Boolean> retainAll(Collection<? extends E1> c) {
        return backingCollection.retainAll(c.stream().map(elementEncoder).collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Boolean> removeAll(Collection<? extends E1> c) {
        return backingCollection.removeAll(c.stream().map(elementEncoder).collect(Collectors.toList()));
    }

    @Override
    public AsyncIterator<E1> iterator() {
        return new TranscodingIterator<>(backingCollection.iterator(), elementDecoder);
    }

    @Override
    public CompletableFuture<Cancellable> listen(CollectionEventListener<E1> listener, Executor executor) {
        return backingCollection.listen(event -> new CollectionEvent<>(event.type(), elementDecoder.apply(event.element())), executor);
    }

    @Override
    public DistributedCollection<E1> sync(Duration operationTimeout) {
        return new BlockingDistributedCollection<>(this, operationTimeout.toMillis());
    }
}