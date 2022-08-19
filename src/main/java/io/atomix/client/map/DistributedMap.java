package io.atomix.client.map;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.set.DistributedSet;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Distributed map.
 */
public interface DistributedMap<K, V> extends SyncPrimitive, Map<K, V> {

    /**
     * Acquires a lock on the given key.
     *
     * @param key the key for which to acquire the lock
     */
    void lock(K key);

    /**
     * Attempts to acquire a lock on the given key.
     *
     * @param key the key for which to acquire the lock
     * @return a boolean indicating whether the lock was successful
     */
    boolean tryLock(K key);

    /**
     * Attempts to acquire a lock on the given key.
     *
     * @param key     the key for which to acquire the lock
     * @param timeout the lock timeout
     * @param unit    the lock unit
     * @return a boolean indicating whether the lock was successful
     */
    default boolean tryLock(K key, long timeout, TimeUnit unit) {
        return tryLock(key, Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * Attempts to acquire a lock on the given key.
     *
     * @param key     the key for which to acquire the lock
     * @param timeout the lock timeout
     * @return a boolean indicating whether the lock was successful
     */
    boolean tryLock(K key, Duration timeout);

    /**
     * Returns a boolean indicating whether a lock is currently held on the given key.
     *
     * @param key the key for which to determine whether a lock exists
     * @return indicates whether a lock exists on the given key
     */
    boolean isLocked(K key);

    /**
     * Releases a lock on the given key.
     *
     * @param key the key for which to release the lock
     */
    void unlock(K key);

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     */
    default Cancellable listen(MapEventListener<K, V> listener) {
        return listen(listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers the specified listener to be notified whenever the map is updated.
     *
     * @param listener listener to notify about map events
     * @param executor executor to use for handling incoming map events
     */
    Cancellable listen(MapEventListener<K, V> listener, Executor executor);

    @Override
    DistributedSet<K> keySet();

    @Override
    DistributedSet<Entry<K, V>> entrySet();

    @Override
    DistributedCollection<V> values();

    @Override
    AsyncDistributedMap<K, V> async();
}
