// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.impl;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.api.runtime.v1.PrimitiveId;
import io.atomix.client.AsyncPrimitive;
import io.atomix.client.Cancellable;
import io.atomix.client.iterator.AsyncIterator;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple asynchronous primitive.
 */
public abstract class AbstractAsyncPrimitive<P extends AsyncPrimitive> implements AsyncPrimitive {
    private final String name;

    protected AbstractAsyncPrimitive(String name) {
        this.name = checkNotNull(name, "primitive name cannot be null");
    }

    @Override
    public String name() {
        return name;
    }

    protected final PrimitiveId id() {
        return PrimitiveId.newBuilder()
                .setName(name())
                .build();
    }

    /**
     * Creates the primitive and connect.
     *
     * @return a future to be completed once the primitive is created and connected
     */
    protected abstract CompletableFuture<P> create(Map<String, String> tags);

    protected <T, U> CompletableFuture<U> execute(BiConsumer<T, StreamObserver<U>> callback, T request) {
        CompletableFuture<U> future = new CompletableFuture<>();
        StreamObserver<U> responseObserver = new StreamObserver<U>() {
            @Override
            public void onNext(U response) {
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
        callback.accept(request, responseObserver);
        return future;
    }

    protected <T, U> CompletableFuture<Cancellable> execute(BiConsumer<T, StreamObserver<U>> callback, T request, Consumer<U> listener, Executor executor) {
        ServerStreamCall<T, U> call = new ServerStreamCall<>(listener, executor);
        return call.call(observer -> callback.accept(request, observer)).thenApply(v -> call);
    }

    private static class ServerStreamCall<T, U> implements Cancellable {
        private final Consumer<U> consumer;
        private final Executor executor;
        private final CompletableFuture<Void> future = new CompletableFuture<>();
        private volatile ClientCallStreamObserver<T> observer;

        public ServerStreamCall(Consumer<U> consumer, Executor executor) {
            this.consumer = consumer;
            this.executor = executor;
        }

        public CompletableFuture<Void> call(Consumer<StreamObserver<U>> caller) {
            caller.accept(new ClientResponseObserver<T, U>() {
                @Override
                public void beforeStart(ClientCallStreamObserver<T> observer) {
                    ServerStreamCall.this.observer = observer;
                    executor.execute(() -> future.complete(null));
                }

                @Override
                public void onNext(U response) {
                    executor.execute(() -> consumer.accept(response));
                }

                @Override
                public void onError(Throwable t) {
                    executor.execute(() -> {
                        if (!future.isDone()) {
                            future.completeExceptionally(t);
                        }
                    });
                }

                @Override
                public void onCompleted() {
                    observer = null;
                }
            });
            return future;
        }

        public void cancel() {
            ClientCallStreamObserver<T> observer = this.observer;
            if (observer != null) {
                observer.cancel(null, null);
            }
        }
    }

    protected <T, U, V> AsyncIterator<V> iterate(BiConsumer<T, StreamObserver<U>> callback, T request, Function<U, V> converter) {
        return new Iterator<>(callback, request, converter);
    }

    private class Iterator<T, U, V> implements AsyncIterator<V>, Consumer<U> {
        private final BiConsumer<T, StreamObserver<U>> callback;
        private final T request;
        private final Function<U, V> converter;
        private volatile CompletableFuture<Void> openFuture;
        private volatile CompletableFuture<V> nextFuture;
        private volatile Cancellable cancel;
        private final Queue<V> entries = new LinkedBlockingQueue<>();

        private Iterator(BiConsumer<T, StreamObserver<U>> callback, T request, Function<U, V> converter) {
            this.callback = callback;
            this.request = request;
            this.converter = converter;
        }

        @Override
        public void accept(U response) {
            if (nextFuture != null) {
                nextFuture.complete(converter.apply(response));
            } else {
                entries.add(converter.apply(response));
            }
        }

        @Override
        public CompletableFuture<Boolean> hasNext() {
            if (openFuture == null) {
                openFuture = execute(callback, request, this, MoreExecutors.directExecutor())
                        .thenAccept(cancellable -> this.cancel = cancellable);
            }
            if (nextFuture == null) {
                nextFuture = new CompletableFuture<>();
                V nextValue = entries.poll();
                if (nextValue != null) {
                    nextFuture.complete(nextValue);
                }
            }
            return openFuture.thenCompose(v -> nextFuture).thenApply(Objects::nonNull);
        }

        @Override
        public CompletableFuture<V> next() {
            return nextFuture.whenComplete((entry, e) -> nextFuture = null);
        }

        @Override
        public CompletableFuture<Void> close() {
            if (cancel != null) {
                cancel.cancel();
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
