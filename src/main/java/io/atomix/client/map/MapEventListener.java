package io.atomix.client.map;

import io.atomix.client.event.EventListener;

/**
 * Listener to be notified about updates to a DistributedMap.
 */
public interface MapEventListener<K, V> extends EventListener<MapEvent<K, V>> {
}
