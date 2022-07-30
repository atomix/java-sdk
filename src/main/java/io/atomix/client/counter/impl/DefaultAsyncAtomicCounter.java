// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter.impl;

import atomix.runtime.counter.v1.CounterGrpc;
import atomix.runtime.counter.v1.CounterOuterClass.GetRequest;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static atomix.runtime.counter.v1.CounterOuterClass.*;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounter
        extends AbstractAsyncPrimitive<CounterGrpc.CounterStub, AsyncAtomicCounter>
        implements AsyncAtomicCounter {

    public DefaultAsyncAtomicCounter(String name, Channel channel) {
        super(name, CounterGrpc.newStub(channel));
    }

    @Override
    protected CompletableFuture<AsyncAtomicCounter> create(Map<String, String> tags) {
        return this.<CreateResponse>execute(observer -> service().create(CreateRequest.newBuilder()
                        .setId(id())
                        .putAllTags(tags)
                        .build(), observer))
                .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return this.<CloseResponse>execute(observer -> service().close(CloseRequest.newBuilder()
                        .setId(id())
                        .build(), observer))
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Long> get() {
        return this.<GetResponse>execute(observer -> service().get(GetRequest.newBuilder()
                        .setId(id())
                        .build(), observer))
                .thenApply(GetResponse::getValue);
    }

    @Override
    public CompletableFuture<Void> set(long value) {
        return this.<SetResponse>execute(observer -> service().set(SetRequest.newBuilder()
                        .setId(id())
                        .setValue(value).build(), observer))
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> compareAndSet(long expectedValue, long updateValue) {
        CompareAndSetRequest compareAndSetRequest = CompareAndSetRequest.newBuilder()
                .setId(id())
                .setCheck(expectedValue)
                .setUpdate(updateValue)
                .build();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.<CompareAndSetResponse>execute(observer -> service().compareAndSet(compareAndSetRequest, observer))
                .whenComplete((response, t) -> {
                    if (t != null) {
                        if (Status.fromThrowable(t).getCode() == Status.ABORTED.getCode()) {
                            future.complete(false);
                        } else {
                            future.completeExceptionally(t);
                        }
                    } else {
                        future.complete(true);
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<Long> addAndGet(long delta) {
        return this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                        .setId(id())
                        .setDelta(delta).build(), observer))
                .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndAdd(long delta) {
        CompletableFuture<Long> getCounter = this.get();
        this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                        .setId(id())
                        .setDelta(delta).build(), observer))
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public CompletableFuture<Long> incrementAndGet() {
        return this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                        .setId(id())
                        .setDelta(1).build(), observer))
                .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndIncrement() {
        CompletableFuture<Long> getCounter = this.get();
        this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                        .setId(id())
                        .setDelta(1).build(), observer))
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public CompletableFuture<Long> decrementAndGet() {
        return this.<DecrementResponse>execute(observer -> service().decrement(DecrementRequest.newBuilder()
                        .setId(id())
                        .setDelta(1).build(), observer))
                .thenApply(DecrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndDecrement() {
        CompletableFuture<Long> getCounter = this.get();
        this.<DecrementResponse>execute(observer -> service().decrement(DecrementRequest.newBuilder()
                        .setId(id())
                        .setDelta(1).build(), observer))
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public AtomicCounter sync(Duration operationTimeout) {
        return new BlockingAtomicCounter(this, operationTimeout.toMillis());
    }
}
