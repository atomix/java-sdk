// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.countermap.impl;

import io.atomix.api.countermap.v1.CounterMapGrpc;
import io.atomix.api.countermap.v1.ClearRequest;
import io.atomix.api.countermap.v1.CloseRequest;
import io.atomix.api.countermap.v1.DecrementRequest;
import io.atomix.api.countermap.v1.DecrementResponse;
import io.atomix.api.countermap.v1.GetRequest;
import io.atomix.api.countermap.v1.GetResponse;
import io.atomix.api.countermap.v1.IncrementRequest;
import io.atomix.api.countermap.v1.IncrementResponse;
import io.atomix.api.countermap.v1.RemoveRequest;
import io.atomix.api.countermap.v1.RemoveResponse;
import io.atomix.api.countermap.v1.SetRequest;
import io.atomix.api.countermap.v1.SetResponse;
import io.atomix.api.countermap.v1.SizeRequest;
import io.atomix.api.countermap.v1.SizeResponse;
import io.atomix.api.countermap.v1.UpdateRequest;
import io.atomix.countermap.AsyncAtomicCounterMap;
import io.atomix.countermap.AtomicCounterMap;
import io.atomix.impl.AbstractAsyncPrimitive;
import io.grpc.Status;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounterMap
    extends AbstractAsyncPrimitive<AsyncAtomicCounterMap<String>, AtomicCounterMap<String>, CounterMapGrpc.CounterMapStub>
    implements AsyncAtomicCounterMap<String> {

    public DefaultAsyncAtomicCounterMap(String name, CounterMapGrpc.CounterMapStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    public CompletableFuture<Void> close() {
        return retry(CounterMapGrpc.CounterMapStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return retry(CounterMapGrpc.CounterMapStub::size, SizeRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(SizeResponse::getSize);
    }

    @Override
    public CompletableFuture<Long> incrementAndGet(String key) {
        return retry(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getPrevValue() + 1);
    }

    @Override
    public CompletableFuture<Long> decrementAndGet(String key) {
        return retry(CounterMapGrpc.CounterMapStub::decrement, DecrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getPrevValue() - 1);
    }

    @Override
    public CompletableFuture<Long> getAndIncrement(String key) {
        return retry(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(IncrementResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> getAndDecrement(String key) {
        return retry(CounterMapGrpc.CounterMapStub::decrement, DecrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(DecrementResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> addAndGet(String key, long delta) {
        return retry(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setDelta(delta)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getPrevValue() + delta);
    }

    @Override
    public CompletableFuture<Long> getAndAdd(String key, long delta) {
        return retry(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setDelta(delta)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(IncrementResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> get(String key) {
        return retry(CounterMapGrpc.CounterMapStub::get, GetRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(GetResponse::getValue)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return 0L;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Long> put(String key, long newValue) {
        return retry(CounterMapGrpc.CounterMapStub::set, SetRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(newValue)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(SetResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> putIfAbsent(String key, long newValue) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> replace(String key, long expectedOldValue, long newValue) {
        return retry(CounterMapGrpc.CounterMapStub::update, UpdateRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(newValue)
            .setPrevValue(expectedOldValue)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(v -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return false;
                } else if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return false;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Long> remove(String key) {
        return retry(CounterMapGrpc.CounterMapStub::remove, RemoveRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(RemoveResponse::getValue);
    }

    @Override
    public CompletableFuture<Boolean> remove(String key, long value) {
        return retry(CounterMapGrpc.CounterMapStub::remove, RemoveRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setPrevValue(value)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(v -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return false;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        return size().thenApply(s -> s == 0);
    }

    @Override
    public CompletableFuture<Void> clear() {
        return retry(CounterMapGrpc.CounterMapStub::clear, ClearRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }
}
