package io.atomix.client.event;

/**
 * Entity capable of receiving events.
 */
@FunctionalInterface
public interface EventListener<E extends Event> extends EventFilter<E> {

    /**
     * Reacts to the specified event.
     *
     * @param event event to be processed
     */
    void event(E event);

}