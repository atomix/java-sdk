// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.impl;

import io.atomix.client.AsyncPrimitive;
import io.atomix.client.Constants;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple asynchronous primitive.
 */
public abstract class AbstractAsyncPrimitive<S, P extends AsyncPrimitive> implements AsyncPrimitive {
    private final String primitiveName;
    private final S service;
    private Context context;

    public AbstractAsyncPrimitive(String primitiveName, String applicationName, String sessionId,
                                  S service, Context context) {
        this.primitiveName = checkNotNull(primitiveName, "primitive name cannot be null");
        this.service = checkNotNull(service, "service cannot be null");
        this.context = checkNotNull(context, "context cannot be null");
        this.context = this.context.withValues(
            Constants.APPLICATION_ID_CTX, checkNotNull(applicationName, "application name cannot be null"),
            Constants.PRIMITIVE_ID_CTX, this.primitiveName,
            Constants.SESSION_ID_CTX, checkNotNull(sessionId, "session id cannot be null"));
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
    public Context context() {
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
        runInCancellableContext(() -> callback.accept(responseObserver));
        return future;
    }

    protected <T> CompletableFuture<Void> execute(Consumer<StreamObserver<T>> callback,
                                                  StreamObserver<T> observer) {
        StreamObserver<T> responseObserver = new StreamObserver<T>() {
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
        };
        runInCancellableContext(() -> callback.accept(responseObserver));
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Executes the given task in the cancellable context of this client.
     *
     * @param task task
     * @throws IllegalStateException if context has been cancelled
     */
    protected void runInCancellableContext(Runnable task) {
        if (this.context.isCancelled()) {
            throw new IllegalStateException(
                    "Context is cancelled (client has been shut down)");
        }
        this.context.run(task);
    }
}
