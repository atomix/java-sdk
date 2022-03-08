// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.concurrent;

/**
 * Thread context factory.
 */
public interface ThreadContextFactory {

    /**
     * Creates a new thread context.
     *
     * @return a new thread context
     */
    ThreadContext createContext();

    /**
     * Closes the factory.
     */
    default void close() {
    }

}
