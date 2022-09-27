// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import io.atomix.api.runtime.countermap.v1.ClearRequest;
import io.atomix.api.runtime.countermap.v1.CloseRequest;
import io.atomix.api.runtime.countermap.v1.CounterMapGrpc;
import io.atomix.api.runtime.countermap.v1.CreateRequest;
import io.atomix.api.runtime.countermap.v1.DecrementRequest;
import io.atomix.api.runtime.countermap.v1.DecrementResponse;
import io.atomix.api.runtime.countermap.v1.GetRequest;
import io.atomix.api.runtime.countermap.v1.GetResponse;
import io.atomix.api.runtime.countermap.v1.IncrementRequest;
import io.atomix.api.runtime.countermap.v1.IncrementResponse;
import io.atomix.api.runtime.countermap.v1.InsertRequest;
import io.atomix.api.runtime.countermap.v1.RemoveRequest;
import io.atomix.api.runtime.countermap.v1.RemoveResponse;
import io.atomix.api.runtime.countermap.v1.SetRequest;
import io.atomix.api.runtime.countermap.v1.SetResponse;
import io.atomix.api.runtime.countermap.v1.SizeRequest;
import io.atomix.api.runtime.countermap.v1.SizeResponse;
import io.atomix.api.runtime.countermap.v1.UpdateRequest;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.map.AsyncAtomicCounterMap;
import io.atomix.client.map.AtomicCounterMap;
import io.grpc.Status;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounterMap
    extends AbstractAsyncPrimitive<CounterMapGrpc.CounterMapStub, AsyncAtomicCounterMap<String>>
    implements AsyncAtomicCounterMap<String> {

    public DefaultAsyncAtomicCounterMap(String name, CounterMapGrpc.CounterMapStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    protected CompletableFuture<AsyncAtomicCounterMap<String>> create(Map<String, String> tags) {
        return execute(CounterMapGrpc.CounterMapStub::create, CreateRequest.newBuilder()
            .setId(id())
            .putAllTags(tags)
            .build())
            .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return execute(CounterMapGrpc.CounterMapStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return execute(CounterMapGrpc.CounterMapStub::size, SizeRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(SizeResponse::getSize);
    }

    @Override
    public CompletableFuture<Long> incrementAndGet(String key) {
        return execute(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getPrevValue() + 1);
    }

    @Override
    public CompletableFuture<Long> decrementAndGet(String key) {
        return execute(CounterMapGrpc.CounterMapStub::decrement, DecrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getPrevValue() - 1);
    }

    @Override
    public CompletableFuture<Long> getAndIncrement(String key) {
        return execute(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(IncrementResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> getAndDecrement(String key) {
        return execute(CounterMapGrpc.CounterMapStub::decrement, DecrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(DecrementResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> addAndGet(String key, long delta) {
        return execute(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setDelta(delta)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> response.getPrevValue() + delta);
    }

    @Override
    public CompletableFuture<Long> getAndAdd(String key, long delta) {
        return execute(CounterMapGrpc.CounterMapStub::increment, IncrementRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setDelta(delta)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(IncrementResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> get(String key) {
        return execute(CounterMapGrpc.CounterMapStub::get, GetRequest.newBuilder()
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
        return execute(CounterMapGrpc.CounterMapStub::set, SetRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(newValue)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(SetResponse::getPrevValue);
    }

    @Override
    public CompletableFuture<Long> putIfAbsent(String key, long newValue) {
        return execute(CounterMapGrpc.CounterMapStub::get, GetRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(GetResponse::getValue)
            .exceptionallyCompose(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return execute(CounterMapGrpc.CounterMapStub::insert, InsertRequest.newBuilder()
                        .setId(id())
                        .setKey(key)
                        .setValue(newValue)
                        .build(), DEFAULT_TIMEOUT)
                        .thenApply(v -> 0L)
                        .exceptionallyCompose(u -> {
                            if (Status.fromThrowable(t).getCode() == Status.Code.ALREADY_EXISTS) {
                                return putIfAbsent(key, newValue);
                            } else {
                                throw (RuntimeException) t;
                            }
                        });
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> replace(String key, long expectedOldValue, long newValue) {
        return execute(CounterMapGrpc.CounterMapStub::update, UpdateRequest.newBuilder()
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
        return execute(CounterMapGrpc.CounterMapStub::remove, RemoveRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(RemoveResponse::getValue);
    }

    @Override
    public CompletableFuture<Boolean> remove(String key, long value) {
        return execute(CounterMapGrpc.CounterMapStub::remove, RemoveRequest.newBuilder()
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
        return execute(CounterMapGrpc.CounterMapStub::clear, ClearRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public AtomicCounterMap<String> sync(Duration operationTimeout) {
        return new BlockingAtomicCounterMap<>(this, operationTimeout.toMillis());
    }
}
