package io.atomix.election;

import com.google.common.base.MoreObjects;
import io.atomix.event.AbstractEvent;

import java.util.Objects;

/**
 * Describes leadership election event.
 */
public class LeadershipEvent<T> extends AbstractEvent<LeadershipEvent.Type, Leadership> {

    /**
     * Type of leadership events.
     */
    public enum Type {
        /**
         * Leader changed event.
         */
        CHANGE,
    }

    private final Leadership<T> oldLeadership;
    private final Leadership<T> newLeadership;

    /**
     * Creates an event of a given type and for the specified instance and the
     * current time.
     *
     * @param type          leadership event type
     * @param oldLeadership previous leadership
     * @param newLeadership new leadership
     */
    public LeadershipEvent(Type type, Leadership<T> oldLeadership, Leadership<T> newLeadership) {
        this(type, oldLeadership, newLeadership, System.currentTimeMillis());
    }

    /**
     * Creates an event of a given type and for the specified subject and time.
     *
     * @param type          leadership event type
     * @param oldLeadership previous leadership
     * @param newLeadership new leadership
     * @param time          occurrence time
     */
    public LeadershipEvent(Type type, Leadership<T> oldLeadership, Leadership<T> newLeadership, long time) {
        super(type, newLeadership, time);
        this.oldLeadership = oldLeadership;
        this.newLeadership = newLeadership;
    }

    /**
     * Returns the prior leadership for the topic.
     *
     * @return the prior leadership for the topic
     */
    public Leadership<T> oldLeadership() {
        return oldLeadership;
    }

    /**
     * Returns the new leadership for the topic.
     *
     * @return the new leadership for the topic
     */
    public Leadership<T> newLeadership() {
        return newLeadership;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type(), subject(), time());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LeadershipEvent) {
            final LeadershipEvent other = (LeadershipEvent) obj;
            return Objects.equals(this.type(), other.type())
                    && Objects.equals(this.subject(), other.subject())
                    && Objects.equals(this.time(), other.time());
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("type", type())
                .add("oldLeadership", oldLeadership())
                .add("newLeadership", newLeadership())
                .add("time", time())
                .toString();
    }
}
