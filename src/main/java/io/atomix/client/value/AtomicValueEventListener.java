package io.atomix.client.value;

import io.atomix.client.event.EventListener;

/**
 * Listener to be notified about updates to a AtomicValue.
 */
public interface AtomicValueEventListener<V> extends EventListener<AtomicValueEvent<V>> {
}
