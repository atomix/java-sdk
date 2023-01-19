// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.counter.impl;

import io.atomix.api.counter.v1.CloseRequest;
import io.atomix.api.counter.v1.CounterGrpc;
import io.atomix.api.counter.v1.DecrementRequest;
import io.atomix.api.counter.v1.DecrementResponse;
import io.atomix.api.counter.v1.GetRequest;
import io.atomix.api.counter.v1.GetResponse;
import io.atomix.api.counter.v1.IncrementRequest;
import io.atomix.api.counter.v1.IncrementResponse;
import io.atomix.api.counter.v1.SetRequest;
import io.atomix.api.counter.v1.UpdateRequest;
import io.atomix.counter.AsyncAtomicCounter;
import io.atomix.counter.AtomicCounter;
import io.atomix.impl.AbstractAsyncPrimitive;
import io.grpc.Status;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounter
    extends AbstractAsyncPrimitive<AsyncAtomicCounter, AtomicCounter, CounterGrpc.CounterStub>
    implements AsyncAtomicCounter {

    public DefaultAsyncAtomicCounter(String name, CounterGrpc.CounterStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    public CompletableFuture<Void> close() {
        return retry(CounterGrpc.CounterStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Long> get() {
        return retry(CounterGrpc.CounterStub::get, GetRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(GetResponse::getValue);
    }

    @Override
    public CompletableFuture<Void> set(long value) {
        return retry(CounterGrpc.CounterStub::set, SetRequest.newBuilder()
            .setId(id())
            .setValue(value).build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> compareAndSet(long expectedValue, long updateValue) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        retry(CounterGrpc.CounterStub::update, UpdateRequest.newBuilder()
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
        return retry(CounterGrpc.CounterStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setDelta(delta).build())
            .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndAdd(long delta) {
        return retry(CounterGrpc.CounterStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setDelta(delta).build())
            .thenApply(response -> response.getValue() - delta);
    }

    @Override
    public CompletableFuture<Long> incrementAndGet() {
        return retry(CounterGrpc.CounterStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setDelta(1).build())
            .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndIncrement() {
        return retry(CounterGrpc.CounterStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setDelta(1).build())
            .thenApply(response -> response.getValue() - 1);
    }

    @Override
    public CompletableFuture<Long> decrementAndGet() {
        return retry(CounterGrpc.CounterStub::decrement, DecrementRequest.newBuilder()
            .setId(id())
            .setDelta(1).build())
            .thenApply(DecrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndDecrement() {
        return retry(CounterGrpc.CounterStub::decrement, DecrementRequest.newBuilder()
            .setId(id())
            .setDelta(1).build())
            .thenApply(response -> response.getValue() + 1);
    }

    @Override
    public AtomicCounter sync(Duration operationTimeout) {
        return new BlockingAtomicCounter(this, operationTimeout);
    }
}
