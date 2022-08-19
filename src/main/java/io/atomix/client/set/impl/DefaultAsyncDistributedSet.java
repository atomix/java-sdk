// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set.impl;

import io.atomix.api.runtime.set.v1.*;
import io.atomix.client.Cancellable;
import io.atomix.client.collection.CollectionEvent;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedSet;
import io.atomix.client.utils.concurrent.Futures;
import io.grpc.Channel;
import io.grpc.Status;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncDistributedSet
        extends AbstractAsyncPrimitive<AsyncDistributedSet<String>>
        implements AsyncDistributedSet<String> {
    private final SetGrpc.SetStub stub;

    public DefaultAsyncDistributedSet(String name, Channel channel) {
        super(name);
        this.stub = SetGrpc.newStub(channel);
    }

    @Override
    protected CompletableFuture<AsyncDistributedSet<String>> create(Map<String, String> tags) {
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
    public CompletableFuture<Boolean> isEmpty() {
        return size().thenApply(size -> size == 0);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return execute(stub::size, SizeRequest.newBuilder()
                .setId(id())
                .build(), DEFAULT_TIMEOUT)
                .thenApply(SizeResponse::getSize);
    }

    @Override
    public CompletableFuture<Boolean> retainAll(Collection<? extends String> c) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> contains(String value) {
        return execute(stub::contains, ContainsRequest.newBuilder()
                .setId(id())
                .setElement(Element.newBuilder()
                        .setValue(value)
                        .build())
                .build(), DEFAULT_TIMEOUT)
                .thenApply(ContainsResponse::getContains);
    }

    @Override
    public CompletableFuture<Boolean> containsAll(Collection<? extends String> c) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (String value : c) {
            futures.add(contains(value));
        }
        return Futures.allOf(futures, (a, b) -> a && b, true);
    }

    @Override
    public CompletableFuture<Boolean> add(String value) {
        return execute(stub::add, AddRequest.newBuilder()
                .setId(id())
                .setElement(Element.newBuilder()
                        .setValue(value)
                        .build())
                .build(), DEFAULT_TIMEOUT)
                .thenApply(response -> true)
                .exceptionally(t -> {
                    if (Status.fromThrowable(t).getCode() == Status.ALREADY_EXISTS.getCode()) {
                        return false;
                    } else {
                        throw (RuntimeException) t;
                    }
                });
    }

    @Override
    public CompletableFuture<Boolean> add(String value, Duration ttl) {
        return execute(stub::add, AddRequest.newBuilder()
                .setId(id())
                .setElement(Element.newBuilder()
                        .setValue(value)
                        .build())
                .setTtl(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(ttl.getSeconds())
                        .setNanos(ttl.getNano())
                        .build())
                .build(), DEFAULT_TIMEOUT)
                .thenApply(response -> true)
                .exceptionally(t -> {
                    if (Status.fromThrowable(t).getCode() == Status.ALREADY_EXISTS.getCode()) {
                        return false;
                    } else {
                        throw (RuntimeException) t;
                    }
                });
    }

    @Override
    public CompletableFuture<Boolean> addAll(Collection<? extends String> c) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (String value : c) {
            futures.add(add(value));
        }
        return Futures.allOf(futures, (a, b) -> a && b, true);
    }

    @Override
    public CompletableFuture<Boolean> remove(String value) {
        return execute(stub::remove, RemoveRequest.newBuilder()
                .setId(id())
                .setElement(Element.newBuilder()
                        .setValue(value)
                        .build())
                .build(), DEFAULT_TIMEOUT)
                .thenApply(response -> true)
                .exceptionally(t -> {
                    if (Status.fromThrowable(t).getCode() == Status.NOT_FOUND.getCode()) {
                        return false;
                    } else {
                        throw (RuntimeException) t;
                    }
                });
    }

    @Override
    public CompletableFuture<Boolean> removeAll(Collection<? extends String> c) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (String value : c) {
            futures.add(remove(value));
        }
        return Futures.allOf(futures, (a, b) -> a && b, true);
    }

    @Override
    public CompletableFuture<Void> clear() {
        return execute(stub::clear, ClearRequest.newBuilder()
                .setId(id())
                .build(), DEFAULT_TIMEOUT)
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Cancellable> listen(CollectionEventListener<String> listener, Executor executor) {
        return execute(stub::events, EventsRequest.newBuilder()
                .setId(id())
                .build(), response -> {
            switch (response.getEvent().getEventCase()) {
                case ADDED:
                    listener.event(new CollectionEvent<>(
                            CollectionEvent.Type.ADD,
                            response.getEvent().getAdded().getElement().getValue()));
                    break;
                case REMOVED:
                    listener.event(new CollectionEvent<>(
                            CollectionEvent.Type.REMOVE,
                            response.getEvent().getRemoved().getElement().getValue()));
                    break;
            }
        }, executor);
    }

    @Override
    public AsyncIterator<String> iterator() {
        return iterate(stub::elements, ElementsRequest.newBuilder()
                .setId(id())
                .build(), response -> response.getElement().getValue());
    }

    @Override
    public DistributedSet<String> sync(Duration operationTimeout) {
        return new BlockingDistributedSet<>(this, operationTimeout.toMillis());
    }
}
