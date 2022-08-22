// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.impl;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.api.runtime.v1.PrimitiveId;
import io.atomix.client.AsyncPrimitive;
import io.atomix.client.Cancellable;
import io.atomix.client.iterator.AsyncIterator;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple asynchronous primitive.
 */
public abstract class AbstractAsyncPrimitive<P extends AsyncPrimitive> implements AsyncPrimitive {
    private static final Duration MAX_DELAY_BETWEEN_RETRIES = Duration.ofSeconds(5);
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);
    private final String name;

    // TODO: Replace the single-threaded executor with a serializing thread pool executor.
    private final ExecutorService streamExecutor = Executors.newSingleThreadExecutor();

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

    @Override
    public CompletableFuture<Void> close() {
        streamExecutor.shutdown();
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates the primitive and connect.
     *
     * @return a future to be completed once the primitive is created and connected
     */
    protected abstract CompletableFuture<P> create(Map<String, String> tags);

    protected <T, U> CompletableFuture<U> execute(BiConsumer<T, StreamObserver<U>> callback, T request) {
        return Retries.retryAsync(
            () -> tryExecute(callback, request),
            t -> Status.fromThrowable(t).getCode() == Status.UNAVAILABLE.getCode(),
            MAX_DELAY_BETWEEN_RETRIES);
    }

    protected <T, U> CompletableFuture<U> execute(BiConsumer<T, StreamObserver<U>> callback, T request, Duration timeout) {
        return Retries.retryAsync(
            () -> tryExecute(callback, request),
            t -> Status.fromThrowable(t).getCode() == Status.UNAVAILABLE.getCode(),
            MAX_DELAY_BETWEEN_RETRIES,
            timeout);
    }

    private <T, U> CompletableFuture<U> tryExecute(BiConsumer<T, StreamObserver<U>> callback, T request) {
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
                observer.cancel("stream closed", null);
            }
        }
    }

    protected <T, U, V> AsyncIterator<V> iterate(BiConsumer<T, StreamObserver<U>> callback, T request, Function<U, V> converter) {
        Iterator<T, U, V> iterator = new Iterator<>(converter, streamExecutor);
        callback.accept(request, iterator);
        return iterator;
    }

    private static class Iterator<T, U, V> implements AsyncIterator<V>, ClientResponseObserver<T, U> {
        private final Executor executor;
        private final Function<U, V> converter;
        private final Queue<V> entries = new LinkedBlockingQueue<>();
        private volatile CompletableFuture<V> nextFuture = new CompletableFuture<>();
        private ClientCallStreamObserver<T> clientCallStreamObserver;
        private boolean complete;
        private Throwable error;
        private boolean closed;

        private Iterator(Function<U, V> converter, Executor executor) {
            this.converter = converter;
            this.executor = executor;
        }

        @Override
        public void beforeStart(ClientCallStreamObserver<T> clientCallStreamObserver) {
            executor.execute(() -> {
                if (closed) {
                    clientCallStreamObserver.cancel("stream closed by client", null);
                } else {
                    this.clientCallStreamObserver = clientCallStreamObserver;
                }
            });
        }

        @Override
        public void onNext(U response) {
            executor.execute(() -> {
                if (!complete) {
                    V value = converter.apply(response);
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
        public CompletableFuture<V> next() {
            return nextFuture.thenApplyAsync(result -> {
                V nextValue = entries.poll();
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
