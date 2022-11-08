package io.atomix.multiset.impl;

import com.google.common.collect.Multiset;
import io.atomix.Cancellable;
import io.atomix.Synchronous;
import io.atomix.collection.AsyncDistributedCollection;
import io.atomix.collection.CollectionEventListener;
import io.atomix.collection.DistributedCollection;
import io.atomix.iterator.SyncIterator;
import io.atomix.iterator.impl.BlockingIterator;
import io.atomix.multiset.AsyncDistributedMultiset;
import io.atomix.multiset.DistributedMultiset;
import io.atomix.set.DistributedSet;
import io.atomix.set.impl.BlockingDistributedSet;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link DistributedMultiset} that merely delegates to a {@link AsyncDistributedMultiset}
 * and waits for the operation to complete.
 *
 * @param <E> set element type
 */
public class BlockingDistributedMultiset<E> extends Synchronous<DistributedCollection<E>, AsyncDistributedCollection<E>> implements DistributedMultiset<E> {

    private final AsyncDistributedMultiset<E> asyncSet;

    public BlockingDistributedMultiset(AsyncDistributedMultiset<E> asyncSet, Duration operationTimeout) {
        super(asyncSet, operationTimeout);
        this.asyncSet = asyncSet;
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
        return new BlockingDistributedSet<>(asyncSet.elementSet(), operationTimeout);
    }

    @Override
    public DistributedSet<Multiset.Entry<E>> entrySet() {
        return new BlockingDistributedSet<>(async().entrySet(), operationTimeout);
    }

    @Override
    public void clear() {
        complete(asyncSet.clear());
    }

    @Override
    public SyncIterator<E> iterator() {
        return new BlockingIterator<>(asyncSet.iterator(), operationTimeout);
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
}
