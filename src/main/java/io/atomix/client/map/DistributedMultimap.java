package io.atomix.client.map;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.AtomixChannel;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.map.impl.DefaultDistributedMultimapBuilder;
import io.atomix.client.set.DistributedMultiset;
import io.atomix.client.set.DistributedSet;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * This provides a synchronous version of the functionality provided by
 * {@link AsyncDistributedMultimap}.  Instead of returning futures this map
 * blocks until the future completes then returns the result.
 */
public interface DistributedMultimap<K, V> extends SyncPrimitive<DistributedMultimap<K, V>, AsyncDistributedMultimap<K, V>>, Multimap<K, V> {

    /**
     * Returns a new DistributedMultimap builder.
     *
     * @param channel the AtomixChannel
     * @return the DistributedMultimap builder
     */
    static <K, V> DistributedMultimapBuilder<K, V> builder(AtomixChannel channel) {
        return new DefaultDistributedMultimapBuilder<>(channel);
    }

    /**
     * Adds the set of key-value pairs of the specified key with each of the
     * values in the iterable if each key-value pair does not already exist,
     * if the pair does exist the behavior is implementation specific.
     * (Same as repeated puts but with efficiency gains.)
     *
     * @param mappings the set of maps to be added
     * @return true if any change in the map results from this call, false otherwise
     */
    boolean putAll(Map<K, Collection<? extends V>> mappings);

    /**
     * Removes the key-value pairs with the specified key and values if they
     * exist. In implementations that allow duplicates each instance of a key
     * will remove one matching entry, which one is not defined. Equivalent to
     * repeated calls to {@code remove()} for each key value pair but more
     * efficient.
     *
     * @param mappings the set of maps to be removed
     * @return true if the map changes because of this call, false otherwise
     */
    boolean removeAll(Map<K, Collection<? extends V>> mappings);

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
}
