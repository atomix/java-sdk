// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.impl;

import atomix.runtime.v1.Primitive;
import io.atomix.client.AsyncPrimitive;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple asynchronous primitive.
 */
public abstract class AbstractAsyncPrimitive<S, P extends AsyncPrimitive> implements AsyncPrimitive {
    private final String name;
    private final S service;

    protected AbstractAsyncPrimitive(String name, S service) {
        this.name = checkNotNull(name, "primitive name cannot be null");
        this.service = checkNotNull(service, "service cannot be null");
    }

    @Override
    public String name() {
        return name;
    }

    protected final Primitive.PrimitiveId id() {
        return Primitive.PrimitiveId.newBuilder()
                .setName(name())
                .build();
    }

    /**
     * Returns the primitive service.
     *
     * @return the primitive service
     */
    protected S service() {
        return service;
    }

    /**
     * Creates the primitive and connect.
     *
     * @return a future to be completed once the primitive is created and connected
     */
    protected abstract CompletableFuture<P> create(Map<String, String> tags);

    protected <T> CompletableFuture<T> execute(Consumer<StreamObserver<T>> callback) {
        CompletableFuture<T> future = new CompletableFuture<>();
        StreamObserver<T> responseObserver = new StreamObserver<T>() {
            @Override
            public void onNext(T response) {
                future.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {

            }
        };
        callback.accept(responseObserver);
        return future;
    }
}
