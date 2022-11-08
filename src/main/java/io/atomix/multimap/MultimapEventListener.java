package io.atomix.multimap;

import io.atomix.event.EventListener;

/**
 * Listener to be notified about updates to a ConsistentMultimap.
 */
public interface MultimapEventListener<K, V> extends EventListener<MultimapEvent<K, V>> {
}
