// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.counter.impl;

import io.atomix.Synchronous;
import io.atomix.counter.AsyncAtomicCounter;
import io.atomix.counter.AtomicCounter;

import java.time.Duration;

/**
 * Default implementation for a {@code AtomicCounter} backed by a {@link AsyncAtomicCounter}.
 */
public class BlockingAtomicCounter extends Synchronous<AtomicCounter, AsyncAtomicCounter> implements AtomicCounter {

    private final AsyncAtomicCounter asyncCounter;

    public BlockingAtomicCounter(AsyncAtomicCounter asyncCounter, Duration operationTimeout) {
        super(asyncCounter, operationTimeout);
        this.asyncCounter = asyncCounter;
    }

    @Override
    public long incrementAndGet() {
        return complete(asyncCounter.incrementAndGet());
    }

    @Override
    public long decrementAndGet() {
        return complete(asyncCounter.decrementAndGet());
    }

    @Override
    public long getAndIncrement() {
        return complete(asyncCounter.getAndIncrement());
    }

    @Override
    public long getAndDecrement() {
        return complete(asyncCounter.getAndDecrement());
    }

    @Override
    public long getAndAdd(long delta) {
        return complete(asyncCounter.getAndAdd(delta));
    }

    @Override
    public long addAndGet(long delta) {
        return complete(asyncCounter.addAndGet(delta));
    }

    @Override
    public void set(long value) {
        complete(asyncCounter.set(value));
    }

    @Override
    public boolean compareAndSet(long expectedValue, long updateValue) {
        return complete(asyncCounter.compareAndSet(expectedValue, updateValue));
    }

    @Override
    public long get() {
        return complete(asyncCounter.get());
    }

    @Override
    public AsyncAtomicCounter async() {
        return asyncCounter;
    }
}
