package io.atomix.value;

import io.atomix.event.EventListener;

/**
 * Listener to be notified about updates to a AtomicValue.
 */
public interface AtomicValueEventListener<V> extends EventListener<AtomicValueEvent<V>> {
}
