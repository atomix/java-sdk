// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import io.atomix.client.AsyncPrimitive;
import io.atomix.client.DistributedPrimitive;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * An async atomic counter dispenses monotonically increasing values.
 */
public interface AsyncDistributedCounter extends AsyncPrimitive {
    /**
     * Increment by one and return the updated value.
     *
     * @return a future to be completed containing the updated value
     */
    CompletableFuture<Long> incrementAndGet();

    /**
     * Decrement by one and return the updated value.
     *
     * @return a future to be completed containing the updated value
     */
    CompletableFuture<Long> decrementAndGet();

    /**
     * Increment by one and return the previous value.
     *
     * @return a future to be completed containing the previous value
     */
    CompletableFuture<Long> getAndIncrement();

    /**
     * Increment by one and return the previous value.
     *
     * @return a future to be completed containing the previous value
     */
    CompletableFuture<Long> getAndDecrement();

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return a future to be completed containing the previous value
     */
    CompletableFuture<Long> getAndAdd(long delta);

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return a future to be completed containing the updated value
     */
    CompletableFuture<Long> addAndGet(long delta);

    /**
     * Returns the current value of the counter without modifying it.
     *
     * @return a future to be completed containing the current value
     */
    CompletableFuture<Long> get();

    @Override
    default DistributedCounter sync() {
        return sync(Duration.ofMillis(DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS));
    }

    @Override
    DistributedCounter sync(Duration operationTimeout);
}
