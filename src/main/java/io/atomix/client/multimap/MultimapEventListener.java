package io.atomix.client.multimap;

import io.atomix.client.event.EventListener;

/**
 * Listener to be notified about updates to a ConsistentMultimap.
 */
public interface MultimapEventListener<K, V> extends EventListener<MultimapEvent<K, V>> {
}
