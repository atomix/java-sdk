// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter.impl;

import io.atomix.client.PrimitiveManagementService;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.counter.AtomicCounterBuilder;
import io.grpc.Channel;
import io.grpc.Context;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicCounterBuilder extends AtomicCounterBuilder {

    public DefaultAtomicCounterBuilder(String primitiveName, String applicationName, String sessionId,
                                       Channel serviceChannel, Context context,
                                       PrimitiveManagementService primitiveManagementService) {
        super(primitiveName, applicationName, sessionId, serviceChannel, context, primitiveManagementService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicCounter> buildAsync() {
        return new DefaultAsyncAtomicCounter(getPrimitiveName(), getApplicationName(), getSessionId(),
                                             getServiceChannel(), getContext())
                .connect()
                .thenApply(AsyncAtomicCounter::sync);
    }
}
