// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map;

import com.google.common.base.MoreObjects;
import io.atomix.event.AbstractEvent;
import io.atomix.time.Versioned;

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
        INSERT,

        /**
         * Existing map entry updated.
         */
        UPDATE,

        /**
         * Entry removed from map.
         */
        REMOVE,
    }

    private final Versioned<V> newValue;
    private final Versioned<V> oldValue;

    /**
     * Creates a new event object.
     *
     * @param type event type
     * @param key key the event concerns
     * @param currentValue new value key is mapped to
     * @param previousValue value that was replaced
     */
    public AtomicMapEvent(Type type, K key, Versioned<V> currentValue, Versioned<V> previousValue) {
        super(type, key);
        this.newValue = currentValue;
        this.oldValue = previousValue;
    }

    /**
     * Returns the key this event concerns.
     *
     * @return the key
     */
    public K key() {
        return subject();
    }

    /**
     * Returns the new value in the map associated with the key. If {@link #type()} returns {@code REMOVE}, this method
     * will return {@code null}.
     *
     * @return the new value for key
     */
    public Versioned<V> newValue() {
        return newValue;
    }

    /**
     * Returns the value associated with the key, before it was updated.
     *
     * @return previous value in map for the key
     */
    public Versioned<V> oldValue() {
        return oldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AtomicMapEvent)) {
            return false;
        }
        AtomicMapEvent<K, V> that = (AtomicMapEvent) o;
        return Objects.equals(this.type(), that.type())
                && Objects.equals(this.key(), that.key())
                && Objects.equals(this.newValue, that.newValue)
                && Objects.equals(this.oldValue, that.oldValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type(), key(), newValue, oldValue);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("type", type())
                .add("key", key())
                .add("newValue", newValue)
                .add("oldValue", oldValue)
                .toString();
    }
}