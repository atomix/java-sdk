package io.atomix.map;

import io.atomix.event.EventListener;

/**
 * Listener to be notified about updates to a DistributedMap.
 */
public interface MapEventListener<K, V> extends EventListener<MapEvent<K, V>> {
}
