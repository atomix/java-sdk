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
package io.atomix.client.set.impl;

import io.atomix.api.primitive.Name;
import io.atomix.api.set.*;
import io.atomix.client.collection.CollectionEvent;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.impl.TranscodingStreamObserver;
import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.iterator.impl.StreamObserverIterator;
import io.atomix.client.session.Session;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedSet;
import io.atomix.client.utils.concurrent.Futures;
import io.atomix.client.utils.concurrent.ThreadContext;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Default distributed set primitive.
 */
public class DefaultAsyncDistributedSet
    extends AbstractAsyncPrimitive<SetServiceGrpc.SetServiceStub, AsyncDistributedSet<String>>
    implements AsyncDistributedSet<String> {
    private volatile CompletableFuture<Long> listenFuture;
    private final Map<CollectionEventListener<String>, Executor> eventListeners = new ConcurrentHashMap<>();

    public DefaultAsyncDistributedSet(Name name, Session session, ThreadContext context) {
        super(name, SetServiceGrpc.newStub(session.getPartition().getChannelFactory().getChannel()), session, context);
    }

    @Override
    public CompletableFuture<Boolean> add(String element) {
        return addAll(Collections.singleton(element));
    }

    @Override
    public CompletableFuture<Boolean> remove(String element) {
        return removeAll(Collections.singleton(element));
    }

    @Override
    public CompletableFuture<Integer> size() {
        return query(
            (header, observer) -> getService().size(SizeRequest.newBuilder()
                .setHeader(header)
                .build(), observer),
            SizeResponse::getHeader)
            .thenApply(response -> response.getSize());
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        return size().thenApply(size -> size == 0);
    }

    @Override
    public CompletableFuture<Boolean> contains(String element) {
        return containsAll(Collections.singleton(element));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Boolean> addAll(Collection<? extends String> c) {

        /*return command(
            (header, observer) -> getService().add(AddRequest.newBuilder()
                .setHeader(header)
                .addAllValues((Collection) c)
                .build(), observer),
            AddResponse::getHeader)
            .thenApply(response -> response.getAdded());*/
        return null;
    }

    @Override
    public CompletableFuture<Boolean> containsAll(Collection<? extends String> c) {
        /*return query(
            (header, observer) -> getService().contains(ContainsRequest.newBuilder()
                .setHeader(header)
                .addAllValues((Collection) c)
                .build(), observer),
            ContainsResponse::getHeader)
            .thenApply(response -> response.getContains());*/
        return null;
    }

    @Override
    public CompletableFuture<Boolean> retainAll(Collection<? extends String> c) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> removeAll(Collection<? extends String> c) {
       /* return command(
            (header, observer) -> getService().remove(RemoveRequest.newBuilder()
                .setHeader(header)
                .addAllValues((Collection) c)
                .build(), observer),
            RemoveResponse::getHeader)
            .thenApply(response -> response.getRemoved());*/
        return null;
    }

    @Override
    public CompletableFuture<Void> clear() {
        return command(
            (header, observer) -> getService().clear(ClearRequest.newBuilder()
                .setHeader(header)
                .build(), observer),
            ClearResponse::getHeader)
            .thenApply(response -> null);
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
                        CollectionEvent<String> event = null;
                        switch (response.getType()) {
                            case ADDED:
                                event = new CollectionEvent<>(
                                    CollectionEvent.Type.ADDED,
                                    response.getValue());
                                break;
                            case REMOVED:
                                event = new CollectionEvent<>(
                                    CollectionEvent.Type.REMOVED,
                                    response.getValue());
                                break;
                        }
                        onEvent(event);
                    }

                    private void onEvent(CollectionEvent<String> event) {
                        eventListeners.forEach((l, e) -> e.execute(() -> l.event(event)));
                    }

                    @Override
                    public void onError(Throwable t) {
                        onCompleted();
                    }

                    @Override
                    public void onCompleted() {
                        synchronized (DefaultAsyncDistributedSet.this) {
                            listenFuture = null;
                        }
                        listen();
                    }
                });
        }
        return listenFuture.thenApply(v -> null);
    }

    @Override
    public synchronized CompletableFuture<Void> addListener(CollectionEventListener<String> listener, Executor executor) {
        eventListeners.put(listener, executor);
        return listen();
    }

    @Override
    public synchronized CompletableFuture<Void> removeListener(CollectionEventListener<String> listener) {
        eventListeners.remove(listener);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public AsyncIterator<String> iterator() {
        StreamObserverIterator<String> iterator = new StreamObserverIterator<>();
        query(
            (header, observer) -> getService().iterate(IterateRequest.newBuilder()
                .setHeader(header)
                .build(), observer),
            IterateResponse::getHeader,
            new TranscodingStreamObserver<>(
                iterator,
                IterateResponse::getValue));
        return iterator;
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
    public DistributedSet<String> sync(Duration operationTimeout) {
        return new BlockingDistributedSet<>(this, operationTimeout.toMillis());
    }
}
