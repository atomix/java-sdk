// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix;

import com.google.common.base.Throwables;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * DistributedPrimitive that is a synchronous (blocking) version of
 * another.
 */
public abstract class Synchronous<S extends SyncPrimitive<S, A>, A extends AsyncPrimitive<A, S>> implements SyncPrimitive<S, A> {

    private final A primitive;
    protected final Duration operationTimeout;

    protected Synchronous(A primitive, Duration operationTimeout) {
        this.primitive = checkNotNull(primitive, "primitive cannot be null");
        this.operationTimeout = operationTimeout;
    }

    @Override
    public String name() {
        return primitive.name();
    }

    @Override
    public void close() {
        complete(primitive.close());
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
