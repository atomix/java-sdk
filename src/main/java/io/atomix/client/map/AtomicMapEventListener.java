package io.atomix.client.map;

import io.atomix.client.event.EventListener;

/**
 * Listener to be notified about updates to a ConsistentMap.
 */
public interface AtomicMapEventListener<K, V> extends EventListener<AtomicMapEvent<K, V>> {
}