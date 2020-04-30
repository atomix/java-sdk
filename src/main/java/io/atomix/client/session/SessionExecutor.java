/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client.session;

import io.atomix.api.headers.RequestHeader;
import io.atomix.api.headers.ResponseHeader;
import io.atomix.api.primitive.Name;
import io.atomix.client.AsyncAtomixClient;
import io.atomix.client.PrimitiveException;
import io.atomix.client.PrimitiveState;
import io.atomix.client.partition.Partition;
import io.atomix.client.utils.concurrent.ThreadContext;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Session operation submitter.
 */
final class SessionExecutor {
    private static final int[] FIBONACCI = new int[]{1, 1, 2, 3, 5};
    private static final Predicate<Throwable> EXCEPTION_PREDICATE = e ->
        e instanceof ConnectException
            || e instanceof TimeoutException
            || e instanceof ClosedChannelException;
    private static final Predicate<Throwable> EXPIRED_PREDICATE = e ->
        e instanceof PrimitiveException.UnknownClient
            || e instanceof PrimitiveException.UnknownSession;
    private static final Predicate<Throwable> CLOSED_PREDICATE = e ->
        e instanceof PrimitiveException.UnknownClient
            || e instanceof PrimitiveException.UnknownSession;

    private final SessionState state;
    private final Partition partition;
    private final SessionSequencer sequencer;
    private final ThreadContext threadContext;
    private final Map<Long, OperationAttempt> attempts = new LinkedHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionExecutor.class);

    SessionExecutor(
        SessionState state,
        SessionSequencer sequencer,
        ThreadContext threadContext,
        Partition partition) {
        this.state = checkNotNull(state, "state cannot be null");
        this.sequencer = checkNotNull(sequencer, "sequencer cannot be null");
        this.threadContext = checkNotNull(threadContext, "threadContext cannot be null");
        this.partition = partition;
    }

    protected <T> CompletableFuture<T> executeCommand(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> responseHeaderFunction) {
        CompletableFuture<T> future = new CompletableFuture<>();
        LOGGER.info("Session Executer invoke command");
        threadContext.execute(() -> invokeCommand(name, function, responseHeaderFunction, future));
        LOGGER.info("Print future in execute command:" + future.toString());
        return future;
    }

    protected <T> CompletableFuture<Long> executeCommand(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> responseHeaderFunction,
        StreamObserver<T> observer) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        threadContext.execute(() -> invokeCommand(name, function, responseHeaderFunction, observer, future));
        return future;
    }

    protected <T> CompletableFuture<T> executeQuery(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> responseHeaderFunction) {
        CompletableFuture<T> future = new CompletableFuture<>();
        threadContext.execute(() -> invokeQuery(name, function, responseHeaderFunction, future));
        return future;
    }

    protected <T> CompletableFuture<Void> executeQuery(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> responseHeaderFunction,
        StreamObserver<T> observer) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        threadContext.execute(() -> invokeQuery(name, function, responseHeaderFunction, observer, future));
        return future;
    }

    /**
     * Submits a command request to the cluster.
     */
    private <T> void invokeCommand(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
        Function<T, ResponseHeader> responseHeaderFunction,
        CompletableFuture<T> future) {
        RequestHeader header = RequestHeader.newBuilder()
                .setName(name)
                .setPartition(partition.id())
                .setSessionId(state.getSessionId())
                .setIndex(state.nextCommandRequest())
                .build();
        invoke(new CommandAttempt<>(sequencer.nextRequest(), requestFunction, header, responseHeaderFunction, future));
    }

    /**
     * Submits a command request to the cluster.
     */
    private <T> void invokeCommand(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
        Function<T, ResponseHeader> responseHeaderFunction,
        StreamObserver<T> observer,
        CompletableFuture<Long> future) {
        RequestHeader header = RequestHeader.newBuilder()
                .setName(name)
                .setPartition(partition.id())
                .setSessionId(state.getSessionId())
                .setIndex(state.nextCommandRequest())
                .build();
        invoke(new CommandStreamAttempt<>(sequencer.nextRequest(), requestFunction, header, responseHeaderFunction, observer, future));
    }

    /**
     * Submits a query request to the cluster.
     */
    private <T> void invokeQuery(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
        Function<T, ResponseHeader> responseHeaderFunction,
        CompletableFuture<T> future) {
        RequestHeader header = RequestHeader.newBuilder()
                .setName(name)
                .setPartition(partition.id())
                .setSessionId(state.getSessionId())
                .setIndex(state.getCommandRequest())
                .build();
        invoke(new QueryAttempt<>(sequencer.nextRequest(), requestFunction, header, responseHeaderFunction, future));
    }

    /**
     * Submits a query request to the cluster.
     */
    private <T> void invokeQuery(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
        Function<T, ResponseHeader> responseHeaderFunction,
        StreamObserver<T> observer,
        CompletableFuture<Void> future) {
        RequestHeader header = RequestHeader.newBuilder()
                .setName(name)
                .setPartition(partition.id())
                .setSessionId(state.getSessionId())
                .setIndex(state.getCommandRequest())
                .build();
        invoke(new QueryStreamAttempt<>(sequencer.nextRequest(), requestFunction, header, responseHeaderFunction, observer, future));
    }

    /**
     * Submits an operation attempt.
     *
     * @param attempt The attempt to submit.
     */
    private void invoke(OperationAttempt<?, ?> attempt) {
        LOGGER.info("Invoke is called" + state.getSessionId() + ":" + state.getState());
        if (state.getState() == PrimitiveState.CLOSED) {
            attempt.fail(new PrimitiveException.ConcurrentModification("session closed"));
        } else {
            attempts.put(attempt.id, attempt);
            LOGGER.info("List of attempts:" + attempts.values().toString());
            attempt.send();
            LOGGER.info("After send");
            attempt.future.whenComplete((r, e) -> attempts.remove(attempt.id));
            LOGGER.info("After when complete");
        }
    }

    /**
     * Resubmits commands starting after the given sequence number.
     * <p>
     * The sequence number from which to resend commands is the <em>request</em> sequence number, not the client-side
     * sequence number. We resend only commands since queries cannot be reliably resent without losing linearizable
     * semantics. Commands are resent by iterating through all pending operation attempts and retrying commands where the
     * sequence number is greater than the given {@code commandSequence} number and the attempt number is less than or
     * equal to the version.
     */
    private void resubmit(long commandSequence, OperationAttempt attempt) {
        for (Map.Entry<Long, OperationAttempt> entry : attempts.entrySet()) {
            OperationAttempt operation = entry.getValue();
            if (operation instanceof CommandAttempt
                && operation.id > commandSequence
                && operation.attempt <= attempt.attempt) {
                operation.retry();
            }
        }
    }

    /**
     * Resubmits pending commands.
     */
    public void reset() {
        threadContext.execute(() -> {
            for (OperationAttempt attempt : attempts.values()) {
                attempt.retry();
            }
        });
    }

    /**
     * Closes the submitter.
     *
     * @return A completable future to be completed with a list of pending operations.
     */
    public CompletableFuture<Void> close() {
        for (OperationAttempt attempt : new ArrayList<>(attempts.values())) {
            attempt.fail(new PrimitiveException.ConcurrentModification("session closed"));
        }
        attempts.clear();
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Operation attempt.
     */
    private abstract class OperationAttempt<T, U> {
        protected final long id;
        protected final BiConsumer<RequestHeader, StreamObserver<T>> requestFunction;
        protected final RequestHeader requestHeader;
        protected final Function<T, ResponseHeader> responseHeaderFunction;
        protected final int attempt;
        protected final CompletableFuture<U> future;

        protected OperationAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            int attempt,
            CompletableFuture<U> future) {
            this.id = id;
            this.requestFunction = requestFunction;
            this.requestHeader = requestHeader;
            this.responseHeaderFunction = responseHeaderFunction;
            this.attempt = attempt;
            this.future = future;
        }

        /**
         * Returns the header for the given response.
         *
         * @param response the response for which to return the header
         * @return the header for the given response
         */
        protected ResponseHeader getHeader(T response) {
            return responseHeaderFunction.apply(response);
        }

        /**
         * Executes the request function.
         *
         * @param observer the response observer
         */
        protected void execute(StreamObserver<T> observer) {
            LOGGER.info("Call request function accept in session executor");
            requestFunction.accept(requestHeader, observer);
            LOGGER.info("After call request function accept");
        }

        /**
         * Executes the request function, returning a single result future.
         *
         * @return the result future
         */
        protected CompletableFuture<T> execute() {
            LOGGER.info("Execute in session Executor");
            CompletableFuture<T> future = new CompletableFuture<>();
            execute(new StreamObserver<T>() {
                @Override
                public void onNext(T response) {
                    LOGGER.info("Response:" + response);
                    boolean completeValue = future.complete(response);
                    LOGGER.info("Complete value" + String.valueOf(completeValue));

                }

                @Override
                public void onError(Throwable t) {
                    LOGGER.info("Error in session executor:" + t.getMessage());
                    future.completeExceptionally(t);
                }

                @Override
                public void onCompleted() {
                    LOGGER.info("Execute is completed");
                }
            });
            LOGGER.info("Future value in session executor:" + future.toString());
            return future;
        }

        /**
         * Sends the attempt.
         */
        protected abstract void send();

        /**
         * Returns the next instance of the attempt.
         *
         * @return The next instance of the attempt.
         */
        protected abstract OperationAttempt<T, U> next();

        /**
         * Returns a new instance of the default exception for the operation.
         *
         * @return A default exception for the operation.
         */
        protected abstract Throwable defaultException();

        /**
         * Completes the operation with an exception.
         *
         * @param error The completion exception.
         */
        protected void complete(Throwable error) {
            sequence(null, () -> future.completeExceptionally(error));
        }

        /**
         * Runs the given callback in proper sequence.
         *
         * @param response The operation response.
         * @param callback The callback to run in sequence.
         */
        protected final void sequence(ResponseHeader response, Runnable callback) {
            sequencer.sequenceResponse(id, response, callback);
        }

        /**
         * Fails the attempt.
         */
        public void fail() {
            fail(defaultException());
        }

        /**
         * Fails the attempt with the given exception.
         *
         * @param t The exception with which to fail the attempt.
         */
        public void fail(Throwable t) {
            sequence(null, () -> {
                state.setCommandResponse(id);
                future.completeExceptionally(t);
            });

            // If the session has been expired or closed, update the client's state.
            if (EXPIRED_PREDICATE.test(t)) {
                state.setState(PrimitiveState.EXPIRED);
            } else if (CLOSED_PREDICATE.test(t)) {
                state.setState(PrimitiveState.CLOSED);
            }
        }

        /**
         * Immediately retries the attempt.
         */
        public void retry() {
            threadContext.execute(() -> invoke(next()));
        }

        /**
         * Retries the attempt after the given duration.
         *
         * @param after The duration after which to retry the attempt.
         */
        public void retry(Duration after) {
            threadContext.schedule(after, () -> invoke(next()));
        }
    }

    /**
     * Command operation attempt.
     */
    private final class CommandAttempt<T> extends OperationAttempt<T, T> implements BiConsumer<T, Throwable> {
        CommandAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            CompletableFuture<T> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, 1, future);
        }

        CommandAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            int attempt,
            CompletableFuture<T> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, attempt, future);
        }

        @Override
        protected void send() {
            LOGGER.info("Send function in session Executer");
            execute().whenComplete(this);
        }

        @Override
        protected CommandAttempt<T> next() {
            return new CommandAttempt<>(id, requestFunction, requestHeader, responseHeaderFunction, this.attempt + 1, future);
        }

        @Override
        protected Throwable defaultException() {
            return new PrimitiveException.CommandFailure("failed to complete command");
        }

        @Override
        public void accept(T response, Throwable error) {
            if (error == null) {
                complete(response);
            } else if (EXPIRED_PREDICATE.test(error) || (error instanceof CompletionException && EXPIRED_PREDICATE.test(error.getCause()))) {
                complete(new PrimitiveException.UnknownSession());
                state.setState(PrimitiveState.EXPIRED);
            } else if (CLOSED_PREDICATE.test(error) || (error instanceof CompletionException && CLOSED_PREDICATE.test(error.getCause()))) {
                complete(new PrimitiveException.ConcurrentModification());
                state.setState(PrimitiveState.CLOSED);
            } else if (EXCEPTION_PREDICATE.test(error) || (error instanceof CompletionException && EXCEPTION_PREDICATE.test(error.getCause()))) {
                retry(Duration.ofSeconds(FIBONACCI[Math.min(attempt - 1, FIBONACCI.length - 1)]));
            } else {
                fail(error);
            }
        }

        /**
         * Completes the response.
         *
         * @param response the response to complete
         */
        @SuppressWarnings("unchecked")
        protected void complete(T response) {
            ResponseHeader header = getHeader(response);
            sequence(header, () -> {
                state.setCommandResponse(id);
                state.setResponseIndex(header.getIndex());
                future.complete(response);
            });
        }
    }

    /**
     * Query operation attempt.
     */
    private final class QueryAttempt<T> extends OperationAttempt<T, T> implements BiConsumer<T, Throwable> {
        QueryAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            CompletableFuture<T> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, 1, future);
        }

        QueryAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            int attempt,
            CompletableFuture<T> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, attempt, future);
        }

        @Override
        protected void send() {
            execute().whenComplete(this);
        }

        @Override
        protected QueryAttempt<T> next() {
            return new QueryAttempt<>(id, requestFunction, requestHeader, responseHeaderFunction, this.attempt + 1, future);
        }

        @Override
        protected Throwable defaultException() {
            return new PrimitiveException.ConcurrentModification("failed to complete query");
        }

        @Override
        public void accept(T response, Throwable error) {
            if (error == null) {
                complete(response);
            } else if (EXPIRED_PREDICATE.test(error) || (error instanceof CompletionException && EXPIRED_PREDICATE.test(error.getCause()))) {
                complete(new PrimitiveException.UnknownSession());
                state.setState(PrimitiveState.EXPIRED);
            } else if (CLOSED_PREDICATE.test(error) || (error instanceof CompletionException && CLOSED_PREDICATE.test(error.getCause()))) {
                complete(new PrimitiveException.ConcurrentModification());
                state.setState(PrimitiveState.CLOSED);
            } else if (EXCEPTION_PREDICATE.test(error) || (error instanceof CompletionException && EXCEPTION_PREDICATE.test(error.getCause()))) {
                complete(new PrimitiveException.ConcurrentModification("Query failed"));
            } else {
                fail(error);
            }
        }

        /**
         * Completes the given response.
         *
         * @param response the response to complete
         */
        @SuppressWarnings("unchecked")
        protected void complete(T response) {
            ResponseHeader header = getHeader(response);
            sequence(header, () -> {
                state.setResponseIndex(header.getIndex());
                future.complete(response);
            });
        }
    }

    /**
     * Command operation attempt.
     */
    private final class CommandStreamAttempt<T>
        extends OperationAttempt<T, Long>
        implements StreamObserver<T> {
        private final StreamObserver<T> responseObserver;
        private final AtomicBoolean complete = new AtomicBoolean();

        CommandStreamAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            StreamObserver<T> responseObserver,
            CompletableFuture<Long> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, 1, future);
            this.responseObserver = responseObserver;
        }

        CommandStreamAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            StreamObserver<T> responseObserver,
            int attempt,
            CompletableFuture<Long> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, attempt, future);
            this.responseObserver = responseObserver;
        }

        @Override
        protected void send() {
            execute(this);
        }

        @Override
        protected CommandStreamAttempt<T> next() {
            return new CommandStreamAttempt<>(id, requestFunction, requestHeader, responseHeaderFunction, responseObserver, this.attempt + 1, future);
        }

        @Override
        protected Throwable defaultException() {
            return new PrimitiveException.CommandFailure("failed to complete command");
        }

        @Override
        public void onNext(T response) {
            /*ResponseHeader header = getHeader(response);
            if (complete.compareAndSet(false, true)) {
                sequence(header, () -> future.complete(header.getIndex()));
            }
            sequencer.sequenceStream(header.getStreams(0), () -> responseObserver.onNext(response));*/
        }

        @Override
        public void onCompleted() {
            /*if (complete.compareAndSet(false, true)) {
                sequence(null, () -> future.complete(null));
            }
            sequencer.closeStream(requestHeader.getSequenceNumber(), () -> responseObserver.onCompleted());*/
        }

        @Override
        public void onError(Throwable error) {
            /*if (complete.compareAndSet(false, true)) {
                sequence(null, () -> future.completeExceptionally(error));
            }
            if (EXPIRED_PREDICATE.test(error) || (error instanceof CompletionException && EXPIRED_PREDICATE.test(error.getCause()))) {
                sequencer.closeStream(requestHeader.getSequenceNumber(), () -> responseObserver.onError(new PrimitiveException.UnknownSession()));
                state.setState(PrimitiveState.EXPIRED);
            } else if (CLOSED_PREDICATE.test(error) || (error instanceof CompletionException && CLOSED_PREDICATE.test(error.getCause()))) {
                sequencer.closeStream(requestHeader.getSequenceNumber(), () -> responseObserver.onError(new PrimitiveException.ConcurrentModification()));
                state.setState(PrimitiveState.CLOSED);
            } else if (EXCEPTION_PREDICATE.test(error) || (error instanceof CompletionException && EXCEPTION_PREDICATE.test(error.getCause()))) {
                retry(Duration.ofSeconds(FIBONACCI[Math.min(attempt - 1, FIBONACCI.length - 1)]));
            } else {
                responseObserver.onError(error);
            }
        }*/
        }

    }

    /**
     * Query operation attempt.
     */
    private final class QueryStreamAttempt<T>
        extends OperationAttempt<T, Void>
        implements StreamObserver<T> {
        private final StreamObserver<T> responseObserver;
        private final AtomicBoolean complete = new AtomicBoolean();

        QueryStreamAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            StreamObserver<T> responseObserver,
            CompletableFuture<Void> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, 1, future);
            this.responseObserver = responseObserver;
        }

        QueryStreamAttempt(
            long id,
            BiConsumer<RequestHeader, StreamObserver<T>> requestFunction,
            RequestHeader requestHeader,
            Function<T, ResponseHeader> responseHeaderFunction,
            StreamObserver<T> responseObserver,
            int attempt,
            CompletableFuture<Void> future) {
            super(id, requestFunction, requestHeader, responseHeaderFunction, attempt, future);
            this.responseObserver = responseObserver;
        }

        @Override
        protected void send() {
            execute(this);
        }

        @Override
        protected QueryStreamAttempt<T> next() {
            return new QueryStreamAttempt<>(id, requestFunction, requestHeader, responseHeaderFunction, responseObserver, this.attempt + 1, future);
        }

        @Override
        protected Throwable defaultException() {
            return new PrimitiveException.ConcurrentModification("failed to complete query");
        }

        @Override
        public void onNext(T response) {
            ResponseHeader header = getHeader(response);
            if (complete.compareAndSet(false, true)) {
                sequence(null, () -> {
                    state.setResponseIndex(header.getIndex());
                    future.complete(null);
                });
            }

            state.setResponseIndex(header.getIndex());
            responseObserver.onNext(response);
        }

        @Override
        public void onCompleted() {
            if (complete.compareAndSet(false, true)) {
                sequence(null, () -> future.complete(null));
            }
            responseObserver.onCompleted();
        }

        @Override
        public void onError(Throwable error) {
            if (complete.compareAndSet(false, true)) {
                sequence(null, () -> future.completeExceptionally(error));
            }
            if (EXPIRED_PREDICATE.test(error) || (error instanceof CompletionException && EXPIRED_PREDICATE.test(error.getCause()))) {
                state.setState(PrimitiveState.EXPIRED);
            } else if (CLOSED_PREDICATE.test(error) || (error instanceof CompletionException && CLOSED_PREDICATE.test(error.getCause()))) {
                state.setState(PrimitiveState.CLOSED);
            } else if (EXCEPTION_PREDICATE.test(error) || (error instanceof CompletionException && EXCEPTION_PREDICATE.test(error.getCause()))) {
                retry(Duration.ofSeconds(FIBONACCI[Math.min(attempt - 1, FIBONACCI.length - 1)]));
            } else {
                responseObserver.onError(error);
            }
        }
    }
}