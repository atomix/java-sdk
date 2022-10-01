package io.atomix.client.election;

import io.atomix.client.event.EventListener;

/**
 * Entity capable of receiving leader elector events.
 */
public interface LeadershipEventListener<T> extends EventListener<LeadershipEvent<T>> {
}
