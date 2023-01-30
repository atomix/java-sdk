// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.value.impl;

import com.google.protobuf.ByteString;
import io.atomix.Cancellable;
import io.atomix.api.value.v1.CloseRequest;
import io.atomix.api.value.v1.EventsRequest;
import io.atomix.api.value.v1.GetRequest;
import io.atomix.api.value.v1.SetRequest;
import io.atomix.api.value.v1.UpdateRequest;
import io.atomix.api.value.v1.ValueGrpc;
import io.atomix.api.value.v1.VersionedValue;
import io.atomix.impl.AbstractAsyncPrimitive;
import io.atomix.time.Versioned;
import io.atomix.value.AsyncAtomicValue;
import io.atomix.value.AtomicValue;
import io.atomix.value.AtomicValueEvent;
import io.atomix.value.AtomicValueEventListener;
import io.grpc.Status;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Atomic value implementation.
 */
public class DefaultAsyncAtomicValue
    extends AbstractAsyncPrimitive<AsyncAtomicValue<String>, AtomicValue<String>, ValueGrpc.ValueStub>
    implements AsyncAtomicValue<String> {

    public DefaultAsyncAtomicValue(String name, ValueGrpc.ValueStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    public CompletableFuture<Void> close() {
        return retry(ValueGrpc.ValueStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Versioned<String>> get() {
        return retry(ValueGrpc.ValueStub::get, GetRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(
                response.getValue().getValue().toStringUtf8(),
                response.getValue().getVersion()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Versioned<String>> set(String value) {
        return retry(ValueGrpc.ValueStub::set, SetRequest.newBuilder()
            .setId(id())
            .setValue(ByteString.copyFromUtf8(value))
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(value, response.getVersion()));
    }

    @Override
    public CompletableFuture<Versioned<String>> set(String value, long version) {
        return retry(ValueGrpc.ValueStub::update, UpdateRequest.newBuilder()
            .setId(id())
            .setValue(ByteString.copyFromUtf8(value))
            .setPrevVersion(version)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(
                response.getPrevValue().getValue().toStringUtf8(),
                response.getPrevValue().getVersion()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
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
                        toVersioned(response.getEvent().getCreated().getValue()), null));
                    break;
                case UPDATED:
                    listener.event(new AtomicValueEvent<>(
                        AtomicValueEvent.Type.UPDATE,
                        toVersioned(response.getEvent().getUpdated().getValue()),
                        toVersioned(response.getEvent().getUpdated().getPrevValue())));
                    break;
                case DELETED:
                    listener.event(new AtomicValueEvent<>(
                        AtomicValueEvent.Type.DELETE,
                        null,
                        toVersioned(response.getEvent().getDeleted().getValue())));
                    break;
            }
        }, executor);
    }

    private static Versioned<String> toVersioned(VersionedValue value) {
        if (value.getVersion() == 0) {
            return null;
        }
        return new Versioned<>(
                value.getValue().toStringUtf8(),
                value.getVersion());
    }
}
