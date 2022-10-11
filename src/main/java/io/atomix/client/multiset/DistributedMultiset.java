package io.atomix.client.multiset;

import com.google.common.collect.Multiset;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.set.DistributedSet;

import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Distributed multiset.
 */
public interface DistributedMultiset<E> extends DistributedCollection<E>, Multiset<E> {

    @Override
    DistributedSet<E> elementSet();

    @Override
    DistributedSet<Entry<E>> entrySet();

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 0);
    }

    @Override
    AsyncDistributedMultiset<E> async();
}
