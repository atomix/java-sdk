// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter.impl;

import io.atomix.api.runtime.counter.v1.*;
import io.atomix.api.runtime.counter.v1.CounterGrpc;
import io.atomix.client.counter.AsyncDistributedCounter;
import io.atomix.client.counter.DistributedCounter;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.grpc.Channel;
import io.grpc.Status;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncDistributedCounter
        extends AbstractAsyncPrimitive<AsyncDistributedCounter>
        implements AsyncDistributedCounter {
    private final CounterGrpc.CounterStub stub;

    public DefaultAsyncDistributedCounter(String name, Channel channel) {
        super(name);
        this.stub = CounterGrpc.newStub(channel);
    }

    @Override
    protected CompletableFuture<AsyncDistributedCounter> create(Map<String, String> tags) {
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
    public DistributedCounter sync(Duration operationTimeout) {
        return new BlockingDistributedCounter(this, operationTimeout.toMillis());
    }
}
