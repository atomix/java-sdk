// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.event;

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