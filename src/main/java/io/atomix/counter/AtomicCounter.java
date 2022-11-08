// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.counter;

import io.atomix.AtomixChannel;
import io.atomix.SyncPrimitive;
import io.atomix.counter.impl.DefaultAtomicCounterBuilder;

/**
 * Distributed version of java.util.concurrent.atomic.AtomicLong.
 */
public interface AtomicCounter extends SyncPrimitive<AtomicCounter, AsyncAtomicCounter> {

    /**
     * Returns a new AtomicCounter builder.
     *
     * @return the AtomicCounter builder
     */
    static AtomicCounterBuilder builder() {
        return builder(AtomixChannel.instance());
    }

    /**
     * Returns a new AtomicCounter builder.
     *
     * @param channel the AtomixChannel
     * @return the AtomicCounter builder
     */
    static AtomicCounterBuilder builder(AtomixChannel channel) {
        return new DefaultAtomicCounterBuilder(channel);
    }

    /**
     * Atomically increment by one and return the updated value.
     *
     * @return updated value
     */
    long incrementAndGet();

    /**
     * Atomically decrement by one and return the updated value.
     *
     * @return updated value
     */
    long decrementAndGet();

    /**
     * Atomically increment by one and return the previous value.
     *
     * @return previous value
     */
    long getAndIncrement();

    /**
     * Atomically decrement by one and return the previous value.
     *
     * @return previous value
     */
    long getAndDecrement();

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return previous value
     */
    long getAndAdd(long delta);

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return updated value
     */
    long addAndGet(long delta);

    /**
     * Atomically sets the given value to the current value.
     *
     * @param value the value to set
     */
    void set(long value);

    /**
     * Atomically sets the given counter to the updated value if the current value is the expected value, otherwise
     * no change occurs.
     *
     * @param expectedValue the expected current value of the counter
     * @param updateValue   the new value to be set
     * @return true if the update occurred and the expected value was equal to the current value, false otherwise
     */
    boolean compareAndSet(long expectedValue, long updateValue);

    /**
     * Returns the current value of the counter without modifying it.
     *
     * @return current value
     */
    long get();

    @Override
    AsyncAtomicCounter async();
}
