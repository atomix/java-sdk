// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter.impl;

import com.google.common.base.Throwables;
import io.atomix.client.PrimitiveException;
import io.atomix.client.Synchronous;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AsyncDistributedCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.counter.DistributedCounter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default implementation for a {@code AtomicCounter} backed by a {@link AsyncAtomicCounter}.
 */
public class BlockingDistributedCounter extends Synchronous<AsyncDistributedCounter> implements DistributedCounter {

    private final AsyncDistributedCounter asyncCounter;
    private final long operationTimeoutMillis;

    public BlockingDistributedCounter(AsyncDistributedCounter asyncCounter, long operationTimeoutMillis) {
        super(asyncCounter);
        this.asyncCounter = asyncCounter;
        this.operationTimeoutMillis = operationTimeoutMillis;
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
    public long get() {
        return complete(asyncCounter.get());
    }

    @Override
    public AsyncDistributedCounter async() {
        return asyncCounter;
    }

    private <T> T complete(CompletableFuture<T> future) {
        try {
            return future.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrimitiveException.Interrupted();
        } catch (TimeoutException e) {
            throw new PrimitiveException.Timeout();
        } catch (ExecutionException e) {
            Throwable cause = Throwables.getRootCause(e);
            if (cause instanceof PrimitiveException) {
                throw (PrimitiveException) cause;
            } else {
                throw new PrimitiveException(cause);
            }
        }
    }
}
