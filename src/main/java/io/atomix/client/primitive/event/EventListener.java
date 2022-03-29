// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.event;

/**
 * Entity capable of receiving events.
 */
@FunctionalInterface
public interface EventListener<E extends Event> {

    /**
     * Reacts to the specified event.
     *
     * @param event event to be processed
     */
    void event(E event);

}
