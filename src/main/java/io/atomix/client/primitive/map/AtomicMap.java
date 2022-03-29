// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.map;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.PrimitiveType;
import io.atomix.client.SyncPrimitive;

import java.util.concurrent.Executor;

/**
 * {@code ConsistentMap} provides the same functionality as {@link AsyncAtomicMap} with
 * the only difference that all its methods block until the corresponding operation completes.
 *
 * @param <K> type of key
 * @param <V> type of value
 */
public interface AtomicMap<K, V> extends SyncPrimitive {
    @Override
    default PrimitiveType type() {
        return AtomicMapType.instance();
    }

    /**
     * Returns the number of entries in the map.
     *
     * @return map size.
     */
    int size();

    /**
     * Returns true if the map is empty.
     *
     * @return true if map has no entries, false otherwise
     */
    boolean isEmpty();

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key key
     * @return true if map contains key, false otherwise
     */
    boolean containsKey(K key);

    /**
     * Returns true if this map contains the specified value.
     *
     * @param value value
     * @return true if map contains value, false otherwise.
     */
    boolean containsValue(V value);

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Returns the value (and version) to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     *
     * @param key the key whose associated value (and version) is to be returned
     * @return the value (and version) to which the specified key is mapped, or null if
     * this map contains no mapping for the key
     *
    Versioned<V> get(K key);
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Returns the value (and version) to which the specified key is mapped, or the provided
     * default value if this map contains no mapping for the key.
     * <p>
     * Note: a non-null {@link Versioned} value will be returned even if the {@code defaultValue}
     * is {@code null}.
     *
     * @param key          the key whose associated value (and version) is to be returned
     * @param defaultValue the default value to return if the key is not set
     * @return the value (and version) to which the specified key is mapped, or null if
     * this map contains no mapping for the key
     *
    Versioned<V> getOrDefault(K key, V defaultValue);
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * If the specified key is not already associated with a value (or is mapped to null),
     * attempts to compute its value using the given mapping function and enters it into
     * this map unless null.
     *
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with the specified key,
     * or null if the computed value is null. Method throws {@code ConsistentMapException.ConcurrentModification}
     * if a concurrent modification of map is detected
     *
    Versioned<V> computeIfAbsent(
            K key, Function<? super K, ? extends V> mappingFunction);
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Attempts to compute a mapping for the specified key and its current mapped value (or
     * null if there is no current mapping).
     * If the computed value is null, the current mapping will be removed from the map.
     *
     * @param key               key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none.
     * This method throws {@code ConsistentMapException.ConcurrentModification}
     * if a concurrent modification of map is detected
     *
    Versioned<V> compute(
            K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * If the value for the specified key is present and non-null, attempts to compute a new
     * mapping given the key and its current mapped value.
     * If the computed value is null, the current mapping will be removed from the map.
     *
     * @param key               key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none.
     * This method throws {@code ConsistentMapException.ConcurrentModification}
     * if a concurrent modification of map is detected
     *
    Versioned<V> computeIfPresent(
            K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * If the value for the specified key satisfies a condition, attempts to compute a new
     * mapping given the key and its current mapped value.
     * If the computed value is null, the current mapping will be removed from the map.
     *
     * @param key               key with which the specified value is to be associated
     * @param condition         condition that should evaluate to true for the computation to proceed
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or the old value if condition evaluates to false.
     * This method throws {@code ConsistentMapException.ConcurrentModification} if a concurrent
     * modification of map is detected
     *
    Versioned<V> computeIf(
            K key, Predicate<? super V> condition, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * If the map previously contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value (and version) associated with key, or null if there was
     * no mapping for key.
     *
    default Versioned<V> put(K key, V value) {
        return put(key, value, Duration.ZERO);
    }
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * If the map previously contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param ttl   the time to live after which to remove the value
     * @return the previous value (and version) associated with key, or null if there was
     * no mapping for key.
     *
    Versioned<V> put(K key, V value, Duration ttl);
     */

    /**
     * Removes the mapping for a key from this map if it is present (optional operation).
     *
     * @param key key whose value is to be removed from the map
     * @return the value (and version) to which this map previously associated the key,
     * or null if the map contained no mapping for the key.
     *
    Versioned<V> remove(K key);
     */

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     */
    void clear();

    // FIXME temporary disabled. We need to implement
    //  the substitute of DistributedSet
    /**
     * Returns a Set view of the keys contained in this map.
     * This method differs from the behavior of java.util.Map.keySet() in that
     * what is returned is a unmodifiable snapshot view of the keys in the ConsistentMap.
     * Attempts to modify the returned set, whether direct or via its iterator,
     * result in an UnsupportedOperationException.
     *
     * @return a set of the keys contained in this map
     *
    DistributedSet<K> keySet();
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of DistributedCollection
    /**
     * Returns the collection of values (and associated versions) contained in this map.
     * This method differs from the behavior of java.util.Map.values() in that
     * what is returned is a unmodifiable snapshot view of the values in the ConsistentMap.
     * Attempts to modify the returned collection, whether direct or via its iterator,
     * result in an UnsupportedOperationException.
     *
     * @return a collection of the values (and associated versions) contained in this map
     *
    DistributedCollection<Versioned<V>> values();
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of DistributedSet
    /**
     * Returns the set of entries contained in this map.
     * This method differs from the behavior of java.util.Map.entrySet() in that
     * what is returned is a unmodifiable snapshot view of the entries in the ConsistentMap.
     * Attempts to modify the returned set, whether direct or via its iterator,
     * result in an UnsupportedOperationException.
     *
     * @return set of entries contained in this map.
     *
    DistributedSet<Entry<K, Versioned<V>>> entrySet();
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * If the specified key is not already associated with a value
     * associates it with the given value and returns null, else returns the current value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key or null
     * if key does not already mapped to a value.
     *
    default Versioned<V> putIfAbsent(K key, V value) {
        return putIfAbsent(key, value, Duration.ZERO);
    }
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * If the specified key is not already associated with a value
     * associates it with the given value and returns null, else returns the current value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param ttl   the time to live after which to remove the value
     * @return the previous value associated with the specified key or null
     * if key does not already mapped to a value.
     *
    Versioned<V> putIfAbsent(K key, V value, Duration ttl);
     */

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * @param key   key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return true if the value was removed
     */
    boolean remove(K key, V value);

    /**
     * Removes the entry for the specified key only if its current
     * version in the map is equal to the specified version.
     *
     * @param key     key with which the specified version is associated
     * @param version version expected to be associated with the specified key
     * @return true if the value was removed
     */
    boolean remove(K key, long version);

    /**
     * Replaces the entry for the specified key only if there is any value
     * which associated with specified key.
     *
     * @param key   key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return the previous value associated with the specified key or null
     *
    Versioned<V> replace(K key, V value);
     */

    /**
     * Replaces the entry for the specified key only if currently mapped
     * to the specified value.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return true if the value was replaced
     *
    boolean replace(K key, V oldValue, V newValue);
     */

    /**
     * Replaces the entry for the specified key only if it is currently mapped to the
     * specified version.
     *
     * @param key        key key with which the specified value is associated
     * @param oldVersion version expected to be associated with the specified key
     * @param newValue   value to be associated with the specified key
     * @return true if the value was replaced
     */
    boolean replace(K key, long oldVersion, V newValue);

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     */
    default void addListener(AtomicMapEventListener<K, V> listener) {
        addListener(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     * @param executor executor to use for handling incoming map events
     */
    void addListener(AtomicMapEventListener<K, V> listener, Executor executor);

    /**
     * Unregisters the specified listener such that it will no longer
     * receive map change notifications.
     *
     * @param listener listener to unregister
     */
    void removeListener(AtomicMapEventListener<K, V> listener);

    @Override
    AsyncAtomicMap<K, V> async();
}
