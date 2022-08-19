package io.atomix.client.set.impl;

import com.google.common.collect.Multiset;
import io.atomix.client.Cancellable;
import io.atomix.client.PrimitiveException;
import io.atomix.client.Synchronous;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.iterator.SyncIterator;
import io.atomix.client.iterator.impl.BlockingIterator;
import io.atomix.client.set.AsyncDistributedMultiset;
import io.atomix.client.set.DistributedMultiset;
import io.atomix.client.set.DistributedSet;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

/**
 * Implementation of {@link io.atomix.client.set.DistributedMultiset} that merely delegates to a {@link AsyncDistributedMultiset}
 * and waits for the operation to complete.
 *
 * @param <E> set element type
 */
public class BlockingDistributedMultiset<E> extends Synchronous<AsyncDistributedMultiset<E>> implements DistributedMultiset<E> {

    private final long operationTimeoutMillis;

    private final AsyncDistributedMultiset<E> asyncSet;

    public BlockingDistributedMultiset(AsyncDistributedMultiset<E> asyncSet, long operationTimeoutMillis) {
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
    public int count(@Nullable Object element) {
        return complete(asyncSet.count(element));
    }

    @Override
    public int add(@Nullable E element, int occurrences) {
        return complete(asyncSet.add(element, occurrences));
    }

    @Override
    public int remove(@Nullable Object element, int occurrences) {
        return complete(asyncSet.remove(element, occurrences));
    }

    @Override
    public int setCount(E element, int count) {
        return complete(asyncSet.setCount(element, count));
    }

    @Override
    public boolean setCount(E element, int oldCount, int newCount) {
        return complete(asyncSet.setCount(element, oldCount, newCount));
    }

    @Override
    public DistributedSet<E> elementSet() {
        return new BlockingDistributedSet<>(asyncSet.elementSet(), operationTimeoutMillis);
    }

    @Override
    public DistributedSet<Multiset.Entry<E>> entrySet() {
        return new BlockingDistributedSet<>(async().entrySet(), operationTimeoutMillis);
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
    public AsyncDistributedMultiset<E> async() {
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
