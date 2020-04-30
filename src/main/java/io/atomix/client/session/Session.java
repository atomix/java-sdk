package io.atomix.client.session;

import io.atomix.api.headers.RequestHeader;
import io.atomix.api.headers.ResponseHeader;
import io.atomix.api.headers.StreamHeader;
import io.atomix.api.primitive.Name;
import io.atomix.api.session.*;
import io.atomix.client.AsyncAtomixClient;
import io.atomix.client.PrimitiveState;
import io.atomix.client.partition.Partition;
import io.atomix.client.utils.concurrent.Futures;
import io.atomix.client.utils.concurrent.Scheduled;
import io.atomix.client.utils.concurrent.ThreadContext;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages a single session.
 */
public class Session {
    private static final double TIMEOUT_FACTOR = .5;
    private static final long MIN_TIMEOUT_DELTA = 2500;

    private final Partition partition;
    private final SessionServiceGrpc.SessionServiceStub service;
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);
    private final ThreadContext context;
    private final Duration timeout;
    private final AtomicBoolean open = new AtomicBoolean();
    private SessionState state;
    private SessionSequencer sequencer;
    private SessionExecutor executor;
    private Scheduled keepAliveTimer;

    Session(Partition partition, ThreadContext context, Duration timeout) {
        this.partition = partition;
        this.context = context;
        this.timeout = timeout;
        this.service = SessionServiceGrpc.newStub(partition.getChannelFactory().getChannel());
    }

    /**
     * Returns the session identifier.
     *
     * @return the session identifier
     */
    public long id() {
        return state.getSessionId();
    }

    /**
     * Returns the session partition.
     *
     * @return the session partition
     */
    public Partition getPartition() {
        return partition;
    }

    private RequestHeader getSessionHeader(Name name) {
        if (state != null) {
            return RequestHeader.newBuilder()
                    .setName(name)
                    .setSessionId(state.getSessionId())
                    .setRequestId(state.getCommandResponse())
                    .setIndex(state.getResponseIndex())
                //.setSequenceNumber(state.getCommandResponse())
                .addAllStreams(sequencer.streams().stream()
                    .map(stream -> StreamHeader.newBuilder()
                        .setStreamId(stream.streamId())
                        //.setIndex(stream.getStreamIndex())
                        //.setLastItemNumber(stream.getStreamSequence())
                        .build())
                    .collect(Collectors.toList()))
                .build();
        } else {
            return RequestHeader.newBuilder()
                .setName(name)
                .build();
        }
    }

    public <T> CompletableFuture<T> session(Name name, BiConsumer<RequestHeader, StreamObserver<T>> function) {
        CompletableFuture<T> future = new CompletableFuture<>();
        function.accept(getSessionHeader(name), new StreamObserver<T>() {
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

    public <T> CompletableFuture<T> command(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction) {

        return executor.executeCommand(name, function, headerFunction);
    }

    public <T> CompletableFuture<Long> command(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction,
        StreamObserver<T> handler) {
        return executor.executeCommand(name, function, headerFunction, handler);
    }

    public <T> CompletableFuture<T> query(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction) {
        return executor.executeQuery(name, function, headerFunction);
    }

    public <T> CompletableFuture<Void> query(
        Name name,
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction,
        StreamObserver<T> handler) {
        return executor.executeQuery(name, function, headerFunction, handler);
    }

    public void state(Consumer<PrimitiveState> consumer) {
        state.addStateChangeListener(consumer);
    }

    /**
     * Connects the session.
     *
     * @return a future to be completed once the session has been connected
     */
    CompletableFuture<Void> connect() {
        if (!open.compareAndSet(false, true)) {
            return Futures.exceptionalFuture(new IllegalStateException());
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        RequestHeader header = RequestHeader.newBuilder().setPartition(partition.id()).build();
        service.openSession(OpenSessionRequest.newBuilder().setHeader(header).build(), new StreamObserver<>() {
            @Override
            public void onNext(OpenSessionResponse response) {
                state = new SessionState(response.getHeader().getSessionId(), timeout.toMillis());
                sequencer = new SessionSequencer(state);
                executor = new SessionExecutor(state, sequencer, context, partition);
                keepAlive(System.currentTimeMillis());
            }

            @Override
            public void onError(Throwable t) {

                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(null);
            }
        });
        return future;
    }

    /**
     * Keeps the primitive session alive.
     */
    private void keepAlive(long lastKeepAliveTime) {
        long keepAliveTime = System.currentTimeMillis();
        keepAlive().whenComplete((succeeded, error) -> {
            if (open.get()) {

                long delta = System.currentTimeMillis() - keepAliveTime;
                // If the keep-alive succeeded, ensure the session state is CONNECTED and schedule another keep-alive.
                if (error == null) {
                    if (succeeded) {
                        state.setState(PrimitiveState.CONNECTED);
                        scheduleKeepAlive(System.currentTimeMillis(), delta);
                    } else {
                        state.setState(PrimitiveState.EXPIRED);
                    }
                }
                // If the keep-alive failed, set the session state to SUSPENDED and schedule another keep-alive.
                else {
                    state.setState(PrimitiveState.SUSPENDED);
                    scheduleKeepAlive(lastKeepAliveTime, delta);
                }
            }
        });
    }

    private CompletableFuture<Boolean> keepAlive() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        RequestHeader header = RequestHeader.newBuilder()
                .setPartition(partition.id())
                .setSessionId(state.getSessionId())
                .build();
        service.keepAlive(KeepAliveRequest.newBuilder().setHeader(header).build(), new StreamObserver<>() {
            @Override
            public void onNext(KeepAliveResponse response) {
                future.complete(true);
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

    /**
     * Schedules a keep-alive request.
     */
    private synchronized void scheduleKeepAlive(long lastKeepAliveTime, long delta) {
        //LOGGER.info("Scheudle Keep Alive is called");
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel();
        }
        Duration delay = Duration.ofMillis(
            Math.max(Math.max((long) (timeout.toMillis() * TIMEOUT_FACTOR) - delta,
                timeout.toMillis() - MIN_TIMEOUT_DELTA - delta), 0));
        keepAliveTimer = context.schedule(delay, () -> {
            if (open.get()) {
                keepAlive(lastKeepAliveTime);
            }
        });
    }

    /**
     * Closes the session.
     *
     * @return a future to be completed once the session has been closed
     */
    CompletableFuture<Void> close() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        RequestHeader header = RequestHeader.newBuilder().setPartition(partition.id()).build();
        service.openSession(OpenSessionRequest.newBuilder().setHeader(header).build(), new StreamObserver<>() {
            @Override
            public void onNext(OpenSessionResponse value) {

            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(null);
            }
        });
        return future;
    }
}
