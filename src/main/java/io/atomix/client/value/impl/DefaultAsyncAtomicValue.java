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
package io.atomix.client.value.impl;

import com.google.protobuf.ByteString;
import io.atomix.api.primitive.Name;
import io.atomix.api.value.*;
import io.atomix.client.Versioned;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.session.Session;
import io.atomix.client.utils.concurrent.ThreadContext;
import io.atomix.client.value.AsyncAtomicValue;
import io.atomix.client.value.AtomicValue;
import io.atomix.client.value.AtomicValueEvent;
import io.atomix.client.value.AtomicValueEventListener;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Default asynchronous atomic value primitive.
 */
public class DefaultAsyncAtomicValue
    extends AbstractAsyncPrimitive<ValueServiceGrpc.ValueServiceStub, AsyncAtomicValue<String>>
    implements AsyncAtomicValue<String> {
    private volatile CompletableFuture<Long> listenFuture;
    private final Set<AtomicValueEventListener<String>> eventListeners = new CopyOnWriteArraySet<>();

    public DefaultAsyncAtomicValue(Name name, Session session, ThreadContext context) {
        super(name, ValueServiceGrpc.newStub(session.getPartition().getChannelFactory().getChannel()), session, context);
    }

    @Override
    public CompletableFuture<Versioned<String>> get() {
        return query(
            (header, observer) -> getService().get(GetRequest.newBuilder()
                .build(), observer),
            GetResponse::getHeader)
            .thenApply(response -> response.getVersion() > 0
                ? new Versioned<>(response.getValue().toStringUtf8(), response.getVersion())
                : null);
    }

    @Override
    public CompletableFuture<Versioned<String>> getAndSet(String value) {
        /*return command(
            (header, observer) -> getService().set(SetRequest.newBuilder()
                .setHeader(header)
                .setValue(ByteString.copyFromUtf8(value))
                .build(), observer), SetResponse::getHeader)
            .thenApply(response -> response.getVersion() > 0
                ? new Versioned<>(response.getPreviousValue().toStringUtf8(), response.getVersion())
                : null);*/
        return null;
    }

    @Override
    public CompletableFuture<Versioned<String>> set(String value) {
        return command(
            (header, observer) -> getService().set(SetRequest.newBuilder()
                .setHeader(header)
                .setValue(ByteString.copyFromUtf8(value))
                .build(), observer), SetResponse::getHeader)
            .thenApply(response -> response.getVersion() > 0
                ? new Versioned<>(value, response.getVersion())
                : null);
    }

    @Override
    public CompletableFuture<Optional<Versioned<String>>> compareAndSet(String expect, String update) {
        /*return command(
            (header, observer) -> getService().checkAndSet(CheckAndSetRequest.newBuilder()
                .setHeader(header)
                .setCheck(ByteString.copyFromUtf8(expect))
                .setUpdate(ByteString.copyFromUtf8(update))
                .build(), observer), CheckAndSetResponse::getHeader)
            .thenApply(response -> Optional.ofNullable(
                response.getSucceeded() ? new Versioned<>(update, response.getVersion()) : null));*/
        return null;
    }

    @Override
    public CompletableFuture<Optional<Versioned<String>>> compareAndSet(long version, String value) {
        /*return command(
            (header, observer) -> getService().checkAndSet(CheckAndSetRequest.newBuilder()
                .setHeader(header)
                .setVersion(version)
                .setUpdate(ByteString.copyFromUtf8(value))
                .build(), observer), CheckAndSetResponse::getHeader)
            .thenApply(response -> Optional.ofNullable(
                response.getSucceeded() ? new Versioned<>(value, response.getVersion()) : null));*/
        return null;
    }

    private synchronized CompletableFuture<Void> listen() {
        if (listenFuture == null && !eventListeners.isEmpty()) {
            listenFuture = command(
                (header, observer) -> getService().events(EventRequest.newBuilder()
                    .setHeader(header)
                    .build(), observer),
                EventResponse::getHeader,
                new StreamObserver<EventResponse>() {
                    @Override
                    public void onNext(EventResponse response) {
                        AtomicValueEvent<String> event = null;
                        switch (response.getType()) {
                            case UPDATED:
                                event = new AtomicValueEvent<>(
                                    AtomicValueEvent.Type.UPDATE,
                                    response.getNewVersion() > 0 ? new Versioned<>(response.getNewValue().toStringUtf8(), response.getNewVersion()) : null,
                                    response.getPreviousVersion() > 0 ? new Versioned<>(response.getPreviousValue().toStringUtf8(), response.getPreviousVersion()) : null);
                                break;
                        }
                        onEvent(event);
                    }

                    private void onEvent(AtomicValueEvent<String> event) {
                        eventListeners.forEach(l -> l.event(event));
                    }

                    @Override
                    public void onError(Throwable t) {
                        onCompleted();
                    }

                    @Override
                    public void onCompleted() {
                        synchronized (DefaultAsyncAtomicValue.this) {
                            listenFuture = null;
                        }
                        listen();
                    }
                });
        }
        return listenFuture.thenApply(v -> null);
    }

    @Override
    public synchronized CompletableFuture<Void> addListener(AtomicValueEventListener<String> listener) {
        eventListeners.add(listener);
        return listen();
    }

    @Override
    public synchronized CompletableFuture<Void> removeListener(AtomicValueEventListener<String> listener) {
        eventListeners.remove(listener);
        return CompletableFuture.completedFuture(null);
    }


    @Override
    protected CompletableFuture<Void> create() {
        return this.<CreateResponse>session((header, observer) -> getService().create(CreateRequest.newBuilder()
            .build(), observer))
            .thenApply(v -> null);
    }

    @Override
    protected CompletableFuture<Void> close(boolean delete) {
        return this.<CloseResponse>session((header, observer) -> getService().close(CloseRequest.newBuilder()
            .setDelete(delete)
            .build(), observer))
            .thenApply(v -> null);
    }

    @Override
    public AtomicValue<String> sync(Duration operationTimeout) {
        return new BlockingAtomicValue<>(this, operationTimeout.toMillis());
    }
}
