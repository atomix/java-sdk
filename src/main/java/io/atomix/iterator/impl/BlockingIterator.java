// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.iterator.impl;

import com.google.common.base.Throwables;
import io.atomix.iterator.AsyncIterator;
import io.atomix.iterator.SyncIterator;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Blocking iterator.
 */
public class BlockingIterator<T> implements SyncIterator<T> {
    private final AsyncIterator<T> asyncIterator;
    private final Duration operationTimeout;

    public BlockingIterator(AsyncIterator<T> asyncIterator, Duration operationTimeout) {
        this.asyncIterator = asyncIterator;
        this.operationTimeout = operationTimeout;
    }

    @Override
    public boolean hasNext() {
        return complete(asyncIterator.hasNext());
    }

    @Override
    public T next() {
        return complete(asyncIterator.next());
    }

    @Override
    public void close() {
        complete(asyncIterator.close());
    }

    @Override
    public AsyncIterator<T> async() {
        return asyncIterator;
    }

    protected <T> T complete(CompletableFuture<T> future) {
        if (operationTimeout == null) {
            return future.join();
        }
        try {
            return future.get(operationTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = Throwables.getRootCause(e);
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }
}