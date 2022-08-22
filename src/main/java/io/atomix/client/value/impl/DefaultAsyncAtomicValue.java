// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.value.impl;

import io.atomix.api.runtime.value.v1.CloseRequest;
import io.atomix.api.runtime.value.v1.CreateRequest;
import io.atomix.api.runtime.value.v1.EventsRequest;
import io.atomix.api.runtime.value.v1.GetRequest;
import io.atomix.api.runtime.value.v1.SetRequest;
import io.atomix.api.runtime.value.v1.UpdateRequest;
import io.atomix.api.runtime.value.v1.ValueGrpc;
import io.atomix.client.Cancellable;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.time.Versioned;
import io.atomix.client.value.AsyncAtomicValue;
import io.atomix.client.value.AtomicValue;
import io.atomix.client.value.AtomicValueEvent;
import io.atomix.client.value.AtomicValueEventListener;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Atomic value implementation.
 */
public class DefaultAsyncAtomicValue
    extends AbstractAsyncPrimitive<ValueGrpc.ValueStub, AsyncAtomicValue<String>>
    implements AsyncAtomicValue<String> {

    public DefaultAsyncAtomicValue(String name, ValueGrpc.ValueStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    protected CompletableFuture<AsyncAtomicValue<String>> create(Map<String, String> tags) {
        return execute(ValueGrpc.ValueStub::create, CreateRequest.newBuilder()
            .setId(id())
            .putAllTags(tags)
            .build())
            .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return execute(ValueGrpc.ValueStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Versioned<String>> get() {
        return execute(ValueGrpc.ValueStub::get, GetRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(
                response.getValue().getValue().toStringUtf8(),
                response.getValue().getVersion()));
    }

    @Override
    public CompletableFuture<Versioned<String>> set(String value) {
        return execute(ValueGrpc.ValueStub::set, SetRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(value, response.getVersion()));
    }

    @Override
    public CompletableFuture<Versioned<String>> set(String value, long version) {
        return execute(ValueGrpc.ValueStub::update, UpdateRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(
                response.getPrevValue().getValue().toStringUtf8(),
                response.getPrevValue().getVersion()));
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicValueEventListener<String> listener, Executor executor) {
        return execute(ValueGrpc.ValueStub::events, EventsRequest.newBuilder()
            .setId(id())
            .build(), response -> {
            switch (response.getEvent().getEventCase()) {
                case CREATED:
                    listener.event(new AtomicValueEvent<>(
                        AtomicValueEvent.Type.CREATE,
                        response.getEvent().getCreated().getValue().getValue().toStringUtf8(), null));
                    break;
                case UPDATED:
                    listener.event(new AtomicValueEvent<>(
                        AtomicValueEvent.Type.UPDATE,
                        response.getEvent().getUpdated().getValue().getValue().toStringUtf8(),
                        response.getEvent().getUpdated().getPrevValue().getValue().toStringUtf8()));
                    break;
                case DELETED:
                    listener.event(new AtomicValueEvent<>(
                        AtomicValueEvent.Type.DELETE,
                        null,
                        response.getEvent().getDeleted().getValue().getValue().toStringUtf8()));
                    break;
            }
        }, executor);
    }

    @Override
    public AtomicValue<String> sync(Duration operationTimeout) {
        return new BlockingAtomicValue<>(this, operationTimeout.toMillis());
    }
}
