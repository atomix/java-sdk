// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import io.atomix.client.SyncPrimitive;

/**
 * Distributed counter.
 */
public interface DistributedCounter extends SyncPrimitive {
    /**
     * Increment by one and return the updated value.
     *
     * @return updated value
     */
    long incrementAndGet();

    /**
     * Decrement by one and return the updated value.
     *
     * @return updated value
     */
    long decrementAndGet();

    /**
     * Increment by one and return the previous value.
     *
     * @return previous value
     */
    long getAndIncrement();

    /**
     * Decrement by one and return the previous value.
     *
     * @return previous value
     */
    long getAndDecrement();

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return previous value
     */
    long getAndAdd(long delta);

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return updated value
     */
    long addAndGet(long delta);

    /**
     * Returns the current value of the counter without modifying it.
     *
     * @return current value
     */
    long get();

    @Override
    AsyncDistributedCounter async();
}
