package io.atomix.client.set;

import io.atomix.client.collection.DistributedCollection;

import java.util.Set;

/**
 * A distributed collection designed for holding unique elements.
 *
 * @param <E> set entry type
 */
public interface DistributedSet<E> extends DistributedCollection<E>, Set<E> {
    @Override
    AsyncDistributedSet<E> async();
}