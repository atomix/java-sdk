// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.map;

import com.google.common.base.MoreObjects;
import io.atomix.client.primitive.event.AbstractEvent;

import java.util.Objects;

/**
 * Representation of a ConsistentMap update notification.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class AtomicMapEvent<K, V> extends AbstractEvent<AtomicMapEvent.Type, K> {

    /**
     * MapEvent type.
     */
    public enum Type {
        /**
         * Entry inserted into the map.
         */
        INSERTED,

        /**
         * Existing map entry updated.
         */
        UPDATED,

        /**
         * Entry removed from map.
         */
        REMOVED
    }

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    //private final Versioned<V> newValue;
    //private final Versioned<V> oldValue;

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Creates a new event object.
     *
     * @param type          event type
     * @param key           key the event concerns
     * @param currentValue  new value key is mapped to
     * @param previousValue value that was replaced
     *
    public AtomicMapEvent(Type type, K key, Versioned<V> currentValue, Versioned<V> previousValue) {
        super(type, key);
        this.newValue = currentValue;
        this.oldValue = previousValue;
    }
     */

    /**
     * Creates a new event object.
     *
     * @param type          event type
     * @param key           key the event concerns
     */
    public AtomicMapEvent(Type type, K key) {
        super(type, key);
    }

    /**
     * Returns the key this event concerns.
     *
     * @return the key
     */
    public K key() {
        return subject();
    }

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Returns the new value in the map associated with the key. If {@link #type()} returns {@code REMOVE}, this method
     * will return {@code null}.
     *
     * @return the new value for key
     *
    public Versioned<V> newValue() {
        return newValue;
    }
     */

    // FIXME temporary disabled. We need to implement
    //  the substitute of Versioned
    /**
     * Returns the value associated with the key, before it was updated.
     *
     * @return previous value in map for the key
     *
    public Versioned<V> oldValue() {
        return oldValue;
    }
     */

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AtomicMapEvent)) {
            return false;
        }

        // FIXME temporary disabled. We need to implement
        //  the substitute of Versioned
        AtomicMapEvent<K, V> that = (AtomicMapEvent) o;
        return Objects.equals(this.type(), that.type())
                && Objects.equals(this.key(), that.key());
                //&& Objects.equals(this.newValue, that.newValue)
                //&& Objects.equals(this.oldValue, that.oldValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type(), key());//, newValue, oldValue);
    }

    @Override
    public String toString() {
        // FIXME temporary disabled. We need to implement
        //  the substitute of Versioned
        return MoreObjects.toStringHelper(getClass())
                .add("type", type())
                .add("key", key())
                //.add("newValue", newValue)
                //.add("oldValue", oldValue)
                .toString();
    }
}
