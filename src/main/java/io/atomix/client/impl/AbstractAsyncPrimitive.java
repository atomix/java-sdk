// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.impl;

import io.atomix.client.AsyncPrimitive;
import io.atomix.client.Cancellable;
import io.atomix.client.SyncPrimitive;
import io.atomix.api.runtime.v1.PrimitiveId;
import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.utils.concurrent.Executors;
import io.atomix.client.utils.concurrent.Retries;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple asynchronous primitive.
 */
public abstract class AbstractAsyncPrimitive<A extends AsyncPrimitive<A, S>, S extends SyncPrimitive<S, A>, T> implements AsyncPrimitive<A, S> {
    protected static final Duration MAX_DELAY_BETWEEN_RETRIES = Duration.ofSeconds(5);
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);
    private final String name;
    private final T stub;
    protected final ScheduledExecutorService executorService;

    protected AbstractAsyncPrimitive(String name, T stub, ScheduledExecutorService executorService) {
        this.name = checkNotNull(name, "primitive name cannot be null");
        this.stub = checkNotNull(stub, "primitive stub cannot be null");
        this.executorService = checkNotNull(executorService, "primitive executor cannot be null");
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
    protected abstract CompletableFuture<A> create(Map<String, String> tags);

    protected <U, V> CompletableFuture<V> retry(StubMethodCall<T, U, V> callback, U request) {
        return Retries.retryAsync(
            () -> execute(callback, request),
            t -> Status.fromThrowable(t).getCode() == Status.UNAVAILABLE.getCode(),
            MAX_DELAY_BETWEEN_RETRIES,
            executorService);
    }

    protected <U, V> CompletableFuture<V> retry(StubMethodCall<T, U, V> callback, U request, Duration timeout) {
        return Retries.retryAsync(
            () -> execute(callback, request),
            t -> Status.fromThrowable(t).getCode() == Status.UNAVAILABLE.getCode(),
            MAX_DELAY_BETWEEN_RETRIES,
            timeout,
            executorService);
    }

    private <U, V> CompletableFuture<V> execute(StubMethodCall<T, U, V> callback, U request) {
        CompletableFuture<V> future = new CompletableFuture<>();
        StreamObserver<V> responseObserver = new StreamObserver<V>() {
            @Override
            public void onNext(V response) {
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
        callback.call(stub, request, responseObserver);
        return future;
    }

    protected <U, V> CompletableFuture<Cancellable> retry(StubMethodCall<T, U, V> callback, U request, Consumer<V> listener, Executor executor) {
        return Retries.retryAsync(
            () -> execute(callback, request, listener, executor),
            t -> Status.fromThrowable(t).getCode() == Status.UNAVAILABLE.getCode(),
            MAX_DELAY_BETWEEN_RETRIES,
            executorService);
    }

    protected <U, V> CompletableFuture<Cancellable> execute(StubMethodCall<T, U, V> callback, U request, Consumer<V> listener, Executor executor) {
        ServerStreamCall<U, V> call = new ServerStreamCall<>(listener, executor);
        return call.call(observer -> callback.call(stub, request, observer)).thenApply(v -> call);
    }

    protected interface StubMethodCall<T, U, V> {
        void call(T stub, U request, StreamObserver<V> streamObserver);
    }

    private static class ServerStreamCall<U, V> implements Cancellable {
        private final Consumer<V> consumer;
        private final Executor executor;
        private final CompletableFuture<Void> future = new CompletableFuture<>();
        private volatile ClientCallStreamObserver<U> observer;

        public ServerStreamCall(Consumer<V> consumer, Executor executor) {
            this.consumer = consumer;
            this.executor = executor;
        }

        public CompletableFuture<Void> call(Consumer<StreamObserver<V>> caller) {
            caller.accept(new ClientResponseObserver<U, V>() {
                @Override
                public void beforeStart(ClientCallStreamObserver<U> observer) {
                    ServerStreamCall.this.observer = observer;
                    executor.execute(() -> future.complete(null));
                }

                @Override
                public void onNext(V response) {
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
            ClientCallStreamObserver<U> observer = this.observer;
            if (observer != null) {
                observer.cancel("stream closed", null);
            }
        }
    }

    protected <U, V, W> AsyncIterator<W> iterate(StubMethodCall<T, U, V> callback, U request, Function<V, W> converter) {
        Iterator<U, V, W> iterator = new Iterator<>(converter, Executors.newSerializingExecutor(executorService));
        callback.call(stub, request, iterator);
        return iterator;
    }

    private static class Iterator<U, V, W> implements AsyncIterator<W>, ClientResponseObserver<U, V> {
        private final Executor executor;
        private final Function<V, W> converter;
        private final Queue<W> entries = new LinkedBlockingQueue<>();
        private volatile CompletableFuture<W> nextFuture = new CompletableFuture<>();
        private ClientCallStreamObserver<U> clientCallStreamObserver;
        private boolean complete;
        private Throwable error;
        private boolean closed;

        private Iterator(Function<V, W> converter, Executor executor) {
            this.converter = converter;
            this.executor = executor;
        }

        @Override
        public void beforeStart(ClientCallStreamObserver<U> clientCallStreamObserver) {
            executor.execute(() -> {
                if (closed) {
                    clientCallStreamObserver.cancel("stream closed by client", null);
                } else {
                    this.clientCallStreamObserver = clientCallStreamObserver;
                }
            });
        }

        @Override
        public void onNext(V response) {
            executor.execute(() -> {
                if (!complete) {
                    W value = converter.apply(response);
                    if (!nextFuture.complete(value)) {
                        entries.add(value);
                    }
                }
            });
        }

        @Override
        public void onError(Throwable throwable) {
            executor.execute(() -> {
                if (!complete) {
                    complete = true;
                    error = throwable;
                    nextFuture.completeExceptionally(throwable);
                }
            });
        }

        @Override
        public void onCompleted() {
            executor.execute(() -> {
                if (!complete) {
                    complete = true;
                    nextFuture.complete(null);
                }
            });
        }

        @Override
        public CompletableFuture<Boolean> hasNext() {
            return nextFuture.thenApply(Objects::nonNull);
        }

        @Override
        public CompletableFuture<W> next() {
            return nextFuture.thenApplyAsync(result -> {
                W nextValue = entries.poll();
                if (nextValue != null) {
                    nextFuture = CompletableFuture.completedFuture(nextValue);
                } else if (complete) {
                    if (error != null) {
                        nextFuture = CompletableFuture.failedFuture(error);
                    } else {
                        nextFuture = CompletableFuture.completedFuture(null);
                    }
                } else {
                    nextFuture = new CompletableFuture<>();
                }
                return result;
            }, executor);
        }

        @Override
        public CompletableFuture<Void> close() {
            CompletableFuture<Void> future = new CompletableFuture<>();
            executor.execute(() -> {
                if (!closed) {
                    closed = true;
                    if (!complete && clientCallStreamObserver != null) {
                        clientCallStreamObserver.cancel("stream closed by client", null);
                    }
                }
                future.complete(null);
            });
            return future;
        }
    }
}
