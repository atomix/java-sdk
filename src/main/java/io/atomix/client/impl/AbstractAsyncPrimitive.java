// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.impl;

import atomix.v1.AtomixOuterClass.PrimitiveId;
import atomix.v1.Headers.RequestHeaders;
import atomix.v1.Headers.ResponseHeaders;
import io.atomix.client.AsyncPrimitive;
import io.atomix.client.utils.ThreadContext;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple asynchronous primitive.
 */
public abstract class AbstractAsyncPrimitive<S, P extends AsyncPrimitive> implements AsyncPrimitive {
    private final String primitiveName;
    private final S service;
    private final ThreadContext context;

    public AbstractAsyncPrimitive(String primitiveName, S service, ThreadContext context) {
        this.primitiveName = checkNotNull(primitiveName);
        this.service = checkNotNull(service);
        this.context = context;
    }

    @Override
    public String name() {
        return primitiveName;
    }

    /**
     * Returns the primitive thread context.
     *
     * @return the primitive thread context
     */
    protected ThreadContext context() {
        return context;
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
    public abstract CompletableFuture<P> connect();

    // FIXME application and client
    private PrimitiveId getPrimitiveId() {
        return PrimitiveId.newBuilder()
                .setPrimitive(primitiveName)
                .setApplication("foo")
                .setClient("bar")
                .build();
    }

    private RequestHeaders getRequestHeader() {
        return RequestHeaders.newBuilder()
                .setPrimitive(getPrimitiveId())
                .build();
    }

    protected <T> CompletableFuture<T> execute(
            BiConsumer<RequestHeaders, StreamObserver<T>> callback,
            Function<T, ResponseHeaders> headerFunction) {
        CompletableFuture<T> future = new CompletableFuture<>();
        callback.accept(getRequestHeader(), new StreamObserver<T>() {
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
        });
        return future;
    }

    protected <T> CompletableFuture<Void> execute(
            BiConsumer<RequestHeaders, StreamObserver<T>> callback,
            Function<T, ResponseHeaders> headerFunction,
            StreamObserver<T> observer) {
        callback.accept(getRequestHeader(), new StreamObserver<T>() {
            @Override
            public void onNext(T response) {
                observer.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onCompleted() {
                observer.onCompleted();
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
