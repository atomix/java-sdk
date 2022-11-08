package io.atomix.election;

import io.atomix.event.EventListener;

/**
 * Entity capable of receiving leader elector events.
 */
public interface LeadershipEventListener<T> extends EventListener<LeadershipEvent<T>> {
}
