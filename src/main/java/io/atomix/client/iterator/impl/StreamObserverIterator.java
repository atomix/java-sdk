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
package io.atomix.client.iterator.impl;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.utils.concurrent.Futures;
import io.grpc.stub.StreamObserver;

/**
 * Stream observer iterator.
 */
public class StreamObserverIterator<T> implements StreamObserver<T>, AsyncIterator<T> {
    private final Queue<CompletableFuture<T>> queue = new ConcurrentLinkedQueue<>();
    private volatile CompletableFuture<T> nextFuture;

    @Override
    public synchronized CompletableFuture<Boolean> hasNext() {
        CompletableFuture<T> future = queue.peek();
        if (future == null) {
            if (nextFuture == null) {
                nextFuture = new CompletableFuture<>();
                queue.add(nextFuture);
            }
            future = nextFuture;
        }
        return future.thenApply(value -> value != null);
    }

    @Override
    public synchronized CompletableFuture<T> next() {
        CompletableFuture<T> future = queue.poll();
        if (future != null) {
            return future;
        } else if (nextFuture != null) {
            return nextFuture;
        } else {
            nextFuture = new CompletableFuture<>();
            return nextFuture;
        }
    }

    @Override
    public synchronized void onNext(T value) {
        if (nextFuture != null) {
            nextFuture.complete(value);
            nextFuture = null;
        } else {
            queue.add(CompletableFuture.completedFuture(value));
        }
    }

    @Override
    public synchronized void onCompleted() {
        if (nextFuture != null) {
            nextFuture.complete(null);
        } else {
            queue.add(CompletableFuture.completedFuture(null));
        }
    }

    @Override
    public synchronized void onError(Throwable error) {
        if (nextFuture != null) {
            nextFuture.completeExceptionally(error);
        } else {
            queue.add(Futures.exceptionalFuture(error));
        }
    }
}
