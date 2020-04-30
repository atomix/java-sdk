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
package io.atomix.client.impl;

import io.atomix.api.headers.RequestHeader;
import io.atomix.api.headers.ResponseHeader;
import io.atomix.api.primitive.Name;
import io.atomix.client.AsyncAtomixClient;
import io.atomix.client.AsyncPrimitive;
import io.atomix.client.ManagedAsyncPrimitive;
import io.atomix.client.PrimitiveState;
import io.atomix.client.session.Session;
import io.atomix.client.utils.concurrent.ThreadContext;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple asynchronous primitive.
 */
public abstract class AbstractAsyncPrimitive<S, P extends AsyncPrimitive> implements ManagedAsyncPrimitive<P> {
    private final Name name;
    private final S service;
    private final Session session;
    private final ThreadContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsyncPrimitive.class);
    public AbstractAsyncPrimitive(Name name, S service, Session session, ThreadContext context) {
        this.name = checkNotNull(name);
        this.service = checkNotNull(service);
        this.session = session;
        this.context = context;
    }

    @Override
    public String name() {
        return name.getName();
    }

    /**
     * Returns the primitive name.
     *
     * @return the primitive name
     */
    protected Name getName() {
        return name;
    }

    /**
     * Returns the primitive thread context.
     *
     * @return the primitive thread context
     */
    protected ThreadContext context() {
        return context;
    }

    /**
     * Returns the primitive service.
     *
     * @return the primitive service
     */
    protected S getService() {
        return service;
    }

    protected <T> CompletableFuture<T> session(BiConsumer<RequestHeader, StreamObserver<T>> function) {
        return session.session(name, function);
    }

    protected <T> CompletableFuture<T> command(
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction) {
        LOGGER.info("Execute command:" + name + ":" + function.toString() + ":" + headerFunction.toString());
        CompletableFuture returnValue = session.command(name, function, headerFunction);
        return returnValue;
    }

    protected <T> CompletableFuture<Long> command(
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction,
        StreamObserver<T> handler) {
        return session.command(name, function, headerFunction, handler);
    }

    protected <T> CompletableFuture<T> query(
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction) {
        return session.query(name, function, headerFunction);
    }

    protected <T> CompletableFuture<Void> query(
        BiConsumer<RequestHeader, StreamObserver<T>> function,
        Function<T, ResponseHeader> headerFunction,
        StreamObserver<T> handler) {
        return session.query(name, function, headerFunction, handler);
    }

    protected void state(Consumer<PrimitiveState> consumer) {
        session.state(consumer);
    }

    @Override
    public CompletableFuture<P> connect() {
        return create().thenApply(v -> (P) this);
    }

    protected abstract CompletableFuture<Void> create();

    @Override
    public CompletableFuture<Void> close() {
        return close(false);
    }

    @Override
    public CompletableFuture<Void> delete() {
        return close(true);
    }

    protected abstract CompletableFuture<Void> close(boolean delete);
}