// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter.impl;

import io.atomix.api.runtime.counter.v1.*;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.grpc.Channel;
import io.grpc.Status;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounter
        extends AbstractAsyncPrimitive<AsyncAtomicCounter>
        implements AsyncAtomicCounter {
    private final CounterGrpc.CounterStub stub;

    public DefaultAsyncAtomicCounter(String name, Channel channel) {
        super(name);
        this.stub = CounterGrpc.newStub(channel);
    }

    @Override
    protected CompletableFuture<AsyncAtomicCounter> create(Map<String, String> tags) {
        return execute(stub::create, CreateRequest.newBuilder()
                .setId(id())
                .putAllTags(tags)
                .build())
                .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return execute(stub::close, CloseRequest.newBuilder()
                .setId(id())
                .build())
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Long> get() {
        return execute(stub::get, GetRequest.newBuilder()
                .setId(id())
                .build())
                .thenApply(GetResponse::getValue);
    }

    @Override
    public CompletableFuture<Void> set(long value) {
        return execute(stub::set, SetRequest.newBuilder()
                .setId(id())
                .setValue(value).build())
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> compareAndSet(long expectedValue, long updateValue) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        execute(stub::update, UpdateRequest.newBuilder()
                .setId(id())
                .setCheck(expectedValue)
                .setUpdate(updateValue)
                .build())
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
        return execute(stub::increment, IncrementRequest.newBuilder()
                .setId(id())
                .setDelta(delta).build())
                .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndAdd(long delta) {
        return execute(stub::increment, IncrementRequest.newBuilder()
                .setId(id())
                .setDelta(delta).build())
                .thenApply(response -> response.getValue() - delta);
    }

    @Override
    public CompletableFuture<Long> incrementAndGet() {
        return execute(stub::increment, IncrementRequest.newBuilder()
                .setId(id())
                .setDelta(1).build())
                .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndIncrement() {
        return execute(stub::increment, IncrementRequest.newBuilder()
                .setId(id())
                .setDelta(1).build())
                .thenApply(response -> response.getValue() - 1);
    }

    @Override
    public CompletableFuture<Long> decrementAndGet() {
        return execute(stub::decrement, DecrementRequest.newBuilder()
                .setId(id())
                .setDelta(1).build())
                .thenApply(DecrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndDecrement() {
        return execute(stub::decrement, DecrementRequest.newBuilder()
                .setId(id())
                .setDelta(1).build())
                .thenApply(response -> response.getValue() + 1);
    }

    @Override
    public AtomicCounter sync(Duration operationTimeout) {
        return new BlockingAtomicCounter(this, operationTimeout.toMillis());
    }
}
