// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter.impl;

import atomix.counter.v1.CounterGrpc;
import atomix.counter.v1.CounterOuterClass.GetRequest;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.grpc.Channel;
import io.grpc.Context;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static atomix.counter.v1.CounterOuterClass.*;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounter
        extends AbstractAsyncPrimitive<CounterGrpc.CounterStub, AsyncAtomicCounter>
        implements AsyncAtomicCounter {

    public DefaultAsyncAtomicCounter(String primitiveName, Channel serviceChannel, Context context) {
        super(primitiveName, CounterGrpc.newStub(serviceChannel), context);
    }

    @Override
    public CompletableFuture<AsyncAtomicCounter> connect() {
        return null;
    }

    @Override
    public CompletableFuture<Void> close() {
        return null;
    }

    @Override
    public CompletableFuture<Void> destroy() {
        return null;
    }

    @Override
    public CompletableFuture<Long> get() {
        return execute((header, observer) -> service().get(GetRequest.newBuilder()
                .setHeaders(header).build(), observer), GetResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue());
    }

    @Override
    public CompletableFuture<Void> set(long value) {
        return execute((header, observer) -> service().set(SetRequest.newBuilder()
                .setHeaders(header)
                .setInput(SetInput.newBuilder().setValue(value).build())
                .build(), observer), SetResponse::getHeaders)
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> compareAndSet(long expectedValue, long updateValue) {
        SetInput setInput = SetInput.newBuilder()
                .addPreconditions(Precondition.newBuilder().setValue(expectedValue).build())
                .setValue(updateValue)
                .build();
        return execute((header, observer) -> service().set(SetRequest.newBuilder()
                .setHeaders(header)
                .setInput(setInput)
                .build(), observer), SetResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue() == updateValue);
    }

    @Override
    public CompletableFuture<Long> addAndGet(long delta) {
        return execute((header, observer) -> service().increment(IncrementRequest.newBuilder()
                .setHeaders(header)
                .setInput(IncrementInput.newBuilder().setDelta(delta).build())
                .build(), observer), IncrementResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue());
    }

    @Override
    public CompletableFuture<Long> getAndAdd(long delta) {
        CompletableFuture<Long> getCounter = execute((header, observer) -> service().get(GetRequest.newBuilder()
                .setHeaders(header)
                .build(), observer), GetResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue());
        execute((header, observer) -> service().increment(IncrementRequest.newBuilder()
                .setHeaders(header)
                .setInput(IncrementInput.newBuilder().setDelta(delta).build())
                .build(), observer), IncrementResponse::getHeaders)
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public CompletableFuture<Long> incrementAndGet() {
        return execute((header, observer) -> service().increment(IncrementRequest.newBuilder()
                .setHeaders(header)
                .setInput(IncrementInput.newBuilder().setDelta(1).build())
                .build(), observer), IncrementResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue());
    }

    @Override
    public CompletableFuture<Long> getAndIncrement() {
        CompletableFuture<Long> getCounter = execute((header, observer) -> service().get(GetRequest.newBuilder()
                .setHeaders(header)
                .build(), observer), GetResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue());
        execute((header, observer) -> service().increment(IncrementRequest.newBuilder()
                .setHeaders(header)
                .setInput(IncrementInput.newBuilder().setDelta(1).build())
                .build(), observer), IncrementResponse::getHeaders)
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public CompletableFuture<Long> decrementAndGet() {
        return execute((header, observer) -> service().decrement(DecrementRequest.newBuilder()
                .setHeaders(header)
                .setInput(DecrementInput.newBuilder().setDelta(1).build())
                .build(), observer), DecrementResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue());
    }

    @Override
    public CompletableFuture<Long> getAndDecrement() {
        CompletableFuture<Long> getCounter = execute((header, observer) -> service().get(GetRequest.newBuilder()
                .setHeaders(header)
                .build(), observer), GetResponse::getHeaders)
                .thenApply(response -> response.getOutput().getValue());
        execute((header, observer) -> service().decrement(DecrementRequest.newBuilder()
                .setHeaders(header)
                .setInput(DecrementInput.newBuilder().setDelta(1).build())
                .build(), observer), DecrementResponse::getHeaders)
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public AtomicCounter sync() {
        return AsyncAtomicCounter.super.sync();
    }

    @Override
    public AtomicCounter sync(Duration operationTimeout) {
        return null;
    }

}
