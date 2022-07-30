// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * DistributedPrimitive that is a synchronous (blocking) version of
 * another.
 *
 * @param <T> type of DistributedPrimitive
 */
public abstract class Synchronous<T extends AsyncPrimitive> implements SyncPrimitive {

    private final T primitive;

    public Synchronous(T primitive) {
        this.primitive = primitive;
    }

    @Override
    public String name() {
        return primitive.name();
    }

    @Override
    public void close() {
        try {
            primitive.close().get(DEFAULT_OPERATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            throw new PrimitiveException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof PrimitiveException) {
                throw (PrimitiveException) e.getCause();
            } else {
                throw new PrimitiveException(e.getCause());
            }
        }
    }
}
