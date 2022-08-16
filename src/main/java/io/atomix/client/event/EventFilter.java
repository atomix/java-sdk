// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.event;

/**
 * Entity capable of filtering events.
 */
public interface EventFilter<E extends Event> {

    /**
     * Indicates whether the specified event is of interest or not.
     * Default implementation always returns true.
     *
     * @param event event to be inspected
     * @return true if event is relevant; false otherwise
     */
    default boolean isRelevant(E event) {
        return true;
    }

}