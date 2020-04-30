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
package io.atomix.client.counter.impl;

import io.atomix.api.counter.*;
import io.atomix.api.primitive.Name;
import io.atomix.client.AsyncAtomixClient;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.session.Session;
import io.atomix.client.utils.concurrent.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounter
    extends AbstractAsyncPrimitive<CounterServiceGrpc.CounterServiceStub, AsyncAtomicCounter>
    implements AsyncAtomicCounter {
    public DefaultAsyncAtomicCounter(Name name, Session session, ThreadContext context) {
        super(name, CounterServiceGrpc.newStub(session.getPartition().getChannelFactory().getChannel()), session, context);
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsyncAtomicCounter.class);

    @Override
    public CompletableFuture<Long> get() {
        LOGGER.info("Get function is called in default async atomic counter");
        return query((header, observer) -> getService().get(GetRequest.newBuilder()
            .setHeader(header)
            .build(), observer), GetResponse::getHeader)
            .thenApply(response -> response.getValue());
    }

    @Override
    public CompletableFuture<Void> set(long value) {
        LOGGER.info("Set function is called");
        return command((header, observer) -> getService().set(SetRequest.newBuilder()
            .setHeader(header)
            .setValue(value)
            .build(), observer), SetResponse::getHeader)
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> compareAndSet(long expectedValue, long updateValue) {
        return command((header, observer) -> getService().checkAndSet(CheckAndSetRequest.newBuilder()
            .setHeader(header)
            .setExpect(expectedValue)
            .setUpdate(updateValue)
            .build(), observer), CheckAndSetResponse::getHeader)
            .thenApply(response -> response.getSucceeded());
    }

    @Override
    public CompletableFuture<Long> addAndGet(long delta) {
        return command((header, observer) -> getService().increment(IncrementRequest.newBuilder()
            .setHeader(header)
            .setDelta(delta)
            .build(), observer), IncrementResponse::getHeader)
            .thenApply(response -> response.getNextValue());
    }

    @Override
    public CompletableFuture<Long> getAndAdd(long delta) {
        return command((header, observer) -> getService().increment(IncrementRequest.newBuilder()
            .setHeader(header)
            .setDelta(delta)
            .build(), observer), IncrementResponse::getHeader)
            .thenApply(response -> response.getPreviousValue());
    }

    @Override
    public CompletableFuture<Long> incrementAndGet() {
        return command((header, observer) -> getService().increment(IncrementRequest.newBuilder()
            .setHeader(header)
            .setDelta(1)
            .build(), observer), IncrementResponse::getHeader)
            .thenApply(response -> response.getNextValue());
    }

    @Override
    public CompletableFuture<Long> getAndIncrement() {
        return command((header, observer) -> getService().increment(IncrementRequest.newBuilder()
            .setHeader(header)
            .setDelta(1)
            .build(), observer), IncrementResponse::getHeader)
            .thenApply(response -> response.getPreviousValue());
    }

    @Override
    public CompletableFuture<Long> decrementAndGet() {
        return command((header, observer) -> getService().decrement(DecrementRequest.newBuilder()
            .setHeader(header)
            .setDelta(1)
            .build(), observer), DecrementResponse::getHeader)
            .thenApply(response -> response.getNextValue());
    }

    @Override
    public CompletableFuture<Long> getAndDecrement() {
        return command((header, observer) -> getService().decrement(DecrementRequest.newBuilder()
            .setHeader(header)
            .setDelta(1)
            .build(), observer), DecrementResponse::getHeader)
            .thenApply(response -> response.getPreviousValue());
    }

    @Override
    protected CompletableFuture<Void> create() {
        LOGGER.info("Create function is called");
        CompletableFuture returnValue = command((header, observer) -> getService().create(CreateRequest.newBuilder()
                .setHeader(header)
                .build(), observer), CreateResponse::getHeader)
                .thenApply(v -> null);
        LOGGER.info("Command Return value" + returnValue.toString());
        return returnValue;
    }

    @Override
    protected CompletableFuture<Void> close(boolean delete) {
        return this.<CloseResponse>session((header, observer) -> getService().close(CloseRequest.newBuilder()
            .setHeader(header)
            .setDelete(delete)
            .build(), observer))
            .thenApply(v -> null);
    }

    @Override
    public AtomicCounter sync(Duration operationTimeout) {
        return new BlockingAtomicCounter(this, operationTimeout.toMillis());
    }
}