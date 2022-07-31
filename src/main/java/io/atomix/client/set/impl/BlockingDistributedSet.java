package io.atomix.client.set.impl;

import io.atomix.client.Cancellable;
import io.atomix.client.PrimitiveException;
import io.atomix.client.Synchronous;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.iterator.SyncIterator;
import io.atomix.client.iterator.impl.BlockingIterator;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedSet;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

/**
 * Implementation of {@link DistributedSet} that merely delegates to a {@link AsyncDistributedSet}
 * and waits for the operation to complete.
 *
 * @param <E> set element type
 */
public class BlockingDistributedSet<E> extends Synchronous<AsyncDistributedSet<E>> implements DistributedSet<E> {

    private final long operationTimeoutMillis;

    private final AsyncDistributedSet<E> asyncSet;

    public BlockingDistributedSet(AsyncDistributedSet<E> asyncSet, long operationTimeoutMillis) {
        super(asyncSet);
        this.asyncSet = asyncSet;
        this.operationTimeoutMillis = operationTimeoutMillis;
    }

    @Override
    public int size() {
        return complete(asyncSet.size());
    }

    @Override
    public boolean isEmpty() {
        return complete(asyncSet.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return complete(asyncSet.contains((E) o));
    }

    @Override
    public boolean add(E e) {
        return complete(asyncSet.add(e));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        return complete(asyncSet.remove((E) o));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsAll(Collection<?> c) {
        return complete(asyncSet.containsAll((Collection<? extends E>) c));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return complete(asyncSet.addAll(c));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection<?> c) {
        return complete(asyncSet.retainAll((Collection<? extends E>) c));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection<?> c) {
        return complete(asyncSet.removeAll((Collection<? extends E>) c));
    }

    @Override
    public void clear() {
        complete(asyncSet.clear());
    }

    @Override
    public SyncIterator<E> iterator() {
        return new BlockingIterator<>(asyncSet.iterator(), operationTimeoutMillis);
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
    public Cancellable listen(CollectionEventListener<E> listener, Executor executor) {
        return complete(asyncSet.listen(listener, executor));
    }

    @Override
    public AsyncDistributedSet<E> async() {
        return asyncSet;
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
            if (e.getCause() instanceof PrimitiveException) {
                throw (PrimitiveException) e.getCause();
            } else if (e.getCause() instanceof NoSuchElementException) {
                throw (NoSuchElementException) e.getCause();
            } else {
                throw new PrimitiveException(e.getCause());
            }
        }
    }
}