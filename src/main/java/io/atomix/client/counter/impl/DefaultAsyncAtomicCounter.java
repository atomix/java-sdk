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

    public DefaultAsyncAtomicCounter(String primitiveName, String applicationName, String sessionId,
                                     Channel serviceChannel, Context context) {
        super(primitiveName, applicationName, sessionId, CounterGrpc.newStub(serviceChannel), context);
    }

    @Override
    public CompletableFuture<AsyncAtomicCounter> connect() {
        return this.<CreateResponse>execute(observer -> service().create(CreateRequest.newBuilder()
                .build(), observer))
                .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return this.<CloseResponse>execute(observer -> service().close(CloseRequest.newBuilder()
                .build(), observer))
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Void> destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Long> get() {
        return this.<GetResponse>execute(observer -> service().get(GetRequest.newBuilder()
                .build(), observer))
                .thenApply(GetResponse::getValue);
    }

    @Override
    public CompletableFuture<Void> set(long value) {
        return this.<SetResponse>execute(observer -> service().set(SetRequest.newBuilder()
                .setValue(value).build(), observer))
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> compareAndSet(long expectedValue, long updateValue) {
        SetRequest setRequest = SetRequest.newBuilder()
                .addPreconditions(Precondition.newBuilder().setValue(expectedValue).build())
                .setValue(updateValue)
                .build();
        return this.<SetResponse>execute(observer -> service().set(setRequest, observer))
                .thenApply(response -> response.getValue() == updateValue);
    }

    @Override
    public CompletableFuture<Long> addAndGet(long delta) {
        return this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                .setDelta(delta).build(), observer))
                .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndAdd(long delta) {
        CompletableFuture<Long> getCounter = this.get();
        this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                .setDelta(delta).build(), observer))
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public CompletableFuture<Long> incrementAndGet() {
        return this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                .setDelta(1).build(), observer))
                .thenApply(IncrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndIncrement() {
        CompletableFuture<Long> getCounter = this.get();
        this.<IncrementResponse>execute(observer -> service().increment(IncrementRequest.newBuilder()
                .setDelta(1).build(), observer))
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public CompletableFuture<Long> decrementAndGet() {
        return this.<DecrementResponse>execute(observer -> service().decrement(DecrementRequest.newBuilder()
                .setDelta(1).build(), observer))
                .thenApply(DecrementResponse::getValue);
    }

    @Override
    public CompletableFuture<Long> getAndDecrement() {
        CompletableFuture<Long> getCounter = this.get();
        this.<DecrementResponse>execute(observer -> service().decrement(DecrementRequest.newBuilder()
                .setDelta(1).build(), observer))
                .thenApply(response -> null);
        return getCounter;
    }

    @Override
    public AtomicCounter sync(Duration operationTimeout) {
        return new BlockingAtomicCounter(this, operationTimeout.toMillis());
    }

}
