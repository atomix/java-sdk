package io.atomix.map;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.Cancellable;
import io.atomix.SyncPrimitive;
import io.atomix.collection.DistributedCollection;
import io.atomix.set.DistributedSet;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Distributed map.
 */
public interface DistributedMap<K, V> extends SyncPrimitive<DistributedMap<K, V>, AsyncDistributedMap<K, V>>, Map<K, V> {

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
}
