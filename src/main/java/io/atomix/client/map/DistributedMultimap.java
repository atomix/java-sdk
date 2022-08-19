package io.atomix.client.map;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.set.DistributedMultiset;
import io.atomix.client.set.DistributedSet;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * This provides a synchronous version of the functionality provided by
 * {@link AsyncDistributedMultimap}.  Instead of returning futures this map
 * blocks until the future completes then returns the result.
 */
public interface DistributedMultimap<K, V> extends SyncPrimitive, Multimap<K, V> {

    /**
     * Returns a set of the keys contained in this multimap with one or more
     * associated values.
     *
     * @return the collection of all keys with one or more associated values,
     * this may be empty
     */
    @Override
    DistributedSet<K> keySet();

    /**
     * Returns a multiset of the keys present in this multimap with one or more
     * associated values each. Keys will appear once for each key-value pair
     * in which they participate.
     *
     * @return a multiset of the keys, this may be empty
     */
    @Override
    DistributedMultiset<K> keys();

    /**
     * Returns a collection of values in the set with duplicates permitted, the
     * size of this collection will equal the size of the map at the time of
     * creation.
     *
     * @return a collection of values, this may be empty
     */
    @Override
    DistributedMultiset<V> values();

    /**
     * Returns a collection of each key-value pair in this map.
     *
     * @return a collection of all entries in the map, this may be empty
     */
    @Override
    DistributedCollection<Map.Entry<K, V>> entries();

    @Override
    DistributedMap<K, Collection<V>> asMap();

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     */
    default Cancellable listen(MultimapEventListener<K, V> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     * @param executor executor to use for handling incoming map events
     */
    Cancellable listen(MultimapEventListener<K, V> listener, Executor executor);

    @Override
    AsyncDistributedMultimap<K, V> async();
}
