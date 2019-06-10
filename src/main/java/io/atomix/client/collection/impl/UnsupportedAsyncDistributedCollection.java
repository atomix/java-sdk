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
package io.atomix.client.collection.impl;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.utils.concurrent.Futures;

/**
 * Distributed collection that returns {@link UnsupportedOperationException} for all asynchronous operations.
 */
public abstract class UnsupportedAsyncDistributedCollection<E> implements AsyncDistributedCollection<E> {
    @Override
    public CompletableFuture<Boolean> add(E element) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> remove(E element) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Integer> size() {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Void> clear() {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> contains(E element) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> addAll(Collection<? extends E> c) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> containsAll(Collection<? extends E> c) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> retainAll(Collection<? extends E> c) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> removeAll(Collection<? extends E> c) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Void> addListener(CollectionEventListener<E> listener, Executor executor) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Void> removeListener(CollectionEventListener<E> listener) {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Void> close() {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Void> delete() {
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }

    @Override
    public DistributedCollection<E> sync(Duration operationTimeout) {
        return new BlockingDistributedCollection<>(this, operationTimeout.toMillis());
    }
}
