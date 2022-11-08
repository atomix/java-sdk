// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import io.atomix.Cancellable;
import io.atomix.api.runtime.map.v1.ClearRequest;
import io.atomix.api.runtime.map.v1.CloseRequest;
import io.atomix.api.runtime.map.v1.CreateRequest;
import io.atomix.api.runtime.map.v1.EntriesRequest;
import io.atomix.api.runtime.map.v1.EventsRequest;
import io.atomix.api.runtime.map.v1.GetRequest;
import io.atomix.api.runtime.map.v1.InsertRequest;
import io.atomix.api.runtime.map.v1.LockRequest;
import io.atomix.api.runtime.map.v1.MapGrpc;
import io.atomix.api.runtime.map.v1.PutRequest;
import io.atomix.api.runtime.map.v1.RemoveRequest;
import io.atomix.api.runtime.map.v1.SizeRequest;
import io.atomix.api.runtime.map.v1.SizeResponse;
import io.atomix.api.runtime.map.v1.UnlockRequest;
import io.atomix.api.runtime.map.v1.UpdateRequest;
import io.atomix.api.runtime.map.v1.VersionedValue;
import io.atomix.collection.AsyncDistributedCollection;
import io.atomix.collection.CollectionEvent;
import io.atomix.collection.CollectionEventListener;
import io.atomix.impl.AbstractAsyncPrimitive;
import io.atomix.iterator.AsyncIterator;
import io.atomix.map.AsyncAtomicMap;
import io.atomix.map.AtomicMap;
import io.atomix.map.AtomicMapEvent;
import io.atomix.map.AtomicMapEventListener;
import io.atomix.set.AsyncDistributedSet;
import io.atomix.time.Versioned;
import io.atomix.util.concurrent.Retries;
import io.grpc.Status;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicMap
    extends AbstractAsyncPrimitive<AsyncAtomicMap<String, byte[]>, AtomicMap<String, byte[]>, MapGrpc.MapStub>
    implements AsyncAtomicMap<String, byte[]> {

    public DefaultAsyncAtomicMap(String name, MapGrpc.MapStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    protected CompletableFuture<AsyncAtomicMap<String, byte[]>> create(Set<String> tags) {
        return retry(MapGrpc.MapStub::create, CreateRequest.newBuilder()
            .setId(id())
            .addAllTags(tags)
            .build())
            .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return retry(MapGrpc.MapStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return retry(MapGrpc.MapStub::size, SizeRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(SizeResponse::getSize);
    }

    @Override
    public CompletableFuture<Boolean> containsKey(String key) {
        return retry(MapGrpc.MapStub::get, GetRequest.newBuilder()
            .setId(id())
            .setKey(key)
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
    public CompletableFuture<Boolean> containsValue(byte[] value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> get(String key) {
        return retry(MapGrpc.MapStub::get, GetRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> toVersioned(response.getValue()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.NOT_FOUND.getCode()) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> getOrDefault(String key, byte[] defaultValue) {
        return get(key).thenApply(value -> {
            if (value == null) {
                return new Versioned<>(defaultValue, 0);
            }
            return value;
        });
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> computeIf(
        String key,
        Predicate<? super byte[]> condition,
        BiFunction<? super String, ? super byte[], ? extends byte[]> remappingFunction) {
        return Retries.retryAsync(
            () -> compute(key, condition, remappingFunction),
            e -> e instanceof ConcurrentModificationException,
            MAX_DELAY_BETWEEN_RETRIES,
            DEFAULT_TIMEOUT,
            executorService);
    }

    private CompletableFuture<Versioned<byte[]>> compute(
        String key,
        Predicate<? super byte[]> condition,
        BiFunction<? super String, ? super byte[], ? extends byte[]> remappingFunction) {
        return get(key).thenCompose(r1 -> {
            byte[] existingValue = r1 == null ? null : r1.value();
            // if the condition evaluates to false, return existing value.
            if (!condition.test(existingValue)) {
                return CompletableFuture.completedFuture(r1);
            }

            byte[] computedValue;
            try {
                computedValue = remappingFunction.apply(key, existingValue);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }

            if (computedValue == null && r1 == null) {
                return CompletableFuture.completedFuture(null);
            }

            CompletableFuture<Versioned<byte[]>> future = new CompletableFuture<>();
            if (r1 == null) {
                retry(MapGrpc.MapStub::insert, InsertRequest.newBuilder()
                    .setId(id())
                    .setKey(key)
                    .setValue(ByteString.copyFrom(computedValue))
                    .build(), DEFAULT_TIMEOUT)
                    .whenComplete((r, t) -> {
                        if (t == null) {
                            future.complete(new Versioned<>(computedValue, r.getVersion()));
                        } else if (Status.fromThrowable(t).getCode() == Status.Code.ALREADY_EXISTS) {
                            future.completeExceptionally(new ConcurrentModificationException());
                        } else {
                            future.completeExceptionally(t);
                        }
                    });
            } else if (computedValue == null) {
                retry(MapGrpc.MapStub::remove, RemoveRequest.newBuilder()
                    .setId(id())
                    .setKey(key)
                    .setPrevVersion(r1.version())
                    .build(), DEFAULT_TIMEOUT)
                    .whenComplete((r, t) -> {
                        if (t == null) {
                            future.complete(new Versioned<>(existingValue, r1.version()));
                        } else if (Status.fromThrowable(t).getCode() == Status.NOT_FOUND.getCode()) {
                            future.completeExceptionally(new ConcurrentModificationException());
                        } else if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                            future.completeExceptionally(new ConcurrentModificationException());
                        } else {
                            future.completeExceptionally(t);
                        }
                    });
            } else {
                retry(MapGrpc.MapStub::update, UpdateRequest.newBuilder()
                    .setId(id())
                    .setKey(key)
                    .setValue(ByteString.copyFrom(computedValue))
                    .setPrevVersion(r1.version())
                    .build(), DEFAULT_TIMEOUT)
                    .whenComplete((r, t) -> {
                        if (t == null) {
                            future.complete(new Versioned<>(computedValue, r.getVersion()));
                        } else if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                            future.completeExceptionally(new ConcurrentModificationException());
                        } else {
                            future.completeExceptionally(t);
                        }
                    });
            }
            return future;
        });
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> put(String key, byte[] value) {
        return retry(MapGrpc.MapStub::put, PutRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(value))
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> toVersioned(response.getPrevValue()));
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> put(String key, byte[] value, Duration ttl) {
        return retry(MapGrpc.MapStub::put, PutRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(value))
            .setTtl(com.google.protobuf.Duration.newBuilder()
                .setSeconds(ttl.getSeconds())
                .setNanos(ttl.getNano())
                .build())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> toVersioned(response.getPrevValue()));
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> putAndGet(String key, byte[] value) {
        return retry(MapGrpc.MapStub::put, PutRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(value))
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(value, response.getVersion()));
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> putAndGet(String key, byte[] value, Duration ttl) {
        return retry(MapGrpc.MapStub::put, PutRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(value))
            .setTtl(com.google.protobuf.Duration.newBuilder()
                .setSeconds(ttl.getSeconds())
                .setNanos(ttl.getNano())
                .build())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(value, response.getVersion()));
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> remove(String key) {
        return retry(MapGrpc.MapStub::remove, RemoveRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> toVersioned(response.getValue()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Void> clear() {
        return retry(MapGrpc.MapStub::clear, ClearRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public AsyncDistributedSet<String> keySet() {
        return new KeySet();
    }

    @Override
    public AsyncDistributedCollection<Versioned<byte[]>> values() {
        return new Values();
    }

    @Override
    public AsyncDistributedSet<Map.Entry<String, Versioned<byte[]>>> entrySet() {
        return new EntrySet();
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> putIfAbsent(String key, byte[] value) {
        return retry(MapGrpc.MapStub::insert, InsertRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(value))
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(value, response.getVersion()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ALREADY_EXISTS) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> putIfAbsent(String key, byte[] value, Duration ttl) {
        return retry(MapGrpc.MapStub::insert, InsertRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(value))
            .setTtl(com.google.protobuf.Duration.newBuilder()
                .setSeconds(ttl.getSeconds())
                .setNanos(ttl.getNano())
                .build())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> new Versioned<>(value, response.getVersion()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ALREADY_EXISTS) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> remove(String key, byte[] value) {
        return get(key).thenCompose(versioned -> {
            if (!Arrays.equals(versioned.value(), value)) {
                return CompletableFuture.completedFuture(false);
            }
            return remove(key, versioned.version());
        });
    }

    @Override
    public CompletableFuture<Boolean> remove(String key, long version) {
        return retry(MapGrpc.MapStub::remove, RemoveRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setPrevVersion(version)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.NOT_FOUND.getCode()) {
                    return false;
                } else if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return false;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Versioned<byte[]>> replace(String key, byte[] value) {
        return retry(MapGrpc.MapStub::update, UpdateRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(value))
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> toVersioned(response.getPrevValue()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> replace(String key, byte[] oldValue, byte[] newValue) {
        return get(key).thenCompose(versioned -> {
            if (!Arrays.equals(versioned.value(), oldValue)) {
                return CompletableFuture.completedFuture(false);
            }
            return replace(key, versioned.version(), newValue);
        });
    }

    @Override
    public CompletableFuture<Boolean> replace(String key, long oldVersion, byte[] newValue) {
        return retry(MapGrpc.MapStub::update, UpdateRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(ByteString.copyFrom(newValue))
            .setPrevVersion(oldVersion)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return false;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Void> lock(String key) {
        return retry(MapGrpc.MapStub::lock, LockRequest.newBuilder()
            .setId(id())
            .addKeys(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> tryLock(String key) {
        return retry(MapGrpc.MapStub::lock, LockRequest.newBuilder()
            .setId(id())
            .addKeys(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return false;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> tryLock(String key, Duration timeout) {
        return retry(MapGrpc.MapStub::lock, LockRequest.newBuilder()
            .setId(id())
            .addKeys(key)
            .setTimeout(com.google.protobuf.Duration.newBuilder()
                .setSeconds(timeout.getSeconds())
                .setNanos(timeout.getNano())
                .build())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return false;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> isLocked(String key) {
        return null;
    }

    @Override
    public CompletableFuture<Void> unlock(String key) {
        return retry(MapGrpc.MapStub::unlock, UnlockRequest.newBuilder()
            .setId(id())
            .addKeys(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicMapEventListener<String, byte[]> listener, Executor executor) {
        return execute(MapGrpc.MapStub::events, EventsRequest.newBuilder()
            .setId(id())
            .build(), response -> {
            switch (response.getEvent().getEventCase()) {
                case INSERTED:
                    listener.event(new AtomicMapEvent<>(
                        AtomicMapEvent.Type.INSERT,
                        response.getEvent().getKey(),
                        toVersioned(response.getEvent().getInserted().getValue()),
                        null));
                    break;
                case UPDATED:
                    listener.event(new AtomicMapEvent<>(
                        AtomicMapEvent.Type.UPDATE,
                        response.getEvent().getKey(),
                        toVersioned(response.getEvent().getUpdated().getValue()),
                        toVersioned(response.getEvent().getUpdated().getPrevValue())));
                    break;
                case REMOVED:
                    listener.event(new AtomicMapEvent<>(
                        AtomicMapEvent.Type.REMOVE,
                        response.getEvent().getKey(),
                        null,
                        toVersioned(response.getEvent().getRemoved().getValue())));
                    break;
            }
        }, executor);
    }

    private static Versioned<byte[]> toVersioned(VersionedValue value) {
        if (value.getVersion() == 0) {
            return null;
        }
        return new Versioned<>(
            value.getValue().toByteArray(),
            value.getVersion());
    }

    private class KeySet implements AsyncDistributedSet<String> {
        @Override
        public String name() {
            return DefaultAsyncAtomicMap.this.name();
        }

        @Override
        public CompletableFuture<Boolean> add(String element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> add(String element, Duration ttl) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(String element) {
            return DefaultAsyncAtomicMap.this.remove(element)
                .thenApply(versioned -> versioned != null);
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncAtomicMap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncAtomicMap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncAtomicMap.this.clear();
        }

        @Override
        public CompletableFuture<Boolean> contains(String element) {
            return containsKey(element);
        }

        @Override
        public CompletableFuture<Boolean> addAll(Collection<? extends String> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> containsAll(Collection<? extends String> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> retainAll(Collection<? extends String> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> removeAll(Collection<? extends String> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Cancellable> listen(CollectionEventListener<String> listener, Executor executor) {
            return DefaultAsyncAtomicMap.this.listen(event -> {
                switch (event.type()) {
                    case INSERT:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.ADD, event.key()));
                        break;
                    case REMOVE:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.REMOVE, event.key()));
                        break;
                    default:
                        break;
                }
            });
        }

        @Override
        public AsyncIterator<String> iterator() {
            return iterate(MapGrpc.MapStub::entries, EntriesRequest.newBuilder()
                .setId(id())
                .build(), response -> response.getEntry().getKey());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncAtomicMap.this.close();
        }
    }

    private class Values implements AsyncDistributedCollection<Versioned<byte[]>> {
        @Override
        public String name() {
            return DefaultAsyncAtomicMap.this.name();
        }

        @Override
        public CompletableFuture<Boolean> add(Versioned<byte[]> element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(Versioned<byte[]> element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncAtomicMap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncAtomicMap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncAtomicMap.this.clear();
        }

        @Override
        public CompletableFuture<Boolean> contains(Versioned<byte[]> element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> addAll(Collection<? extends Versioned<byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> containsAll(Collection<? extends Versioned<byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> retainAll(Collection<? extends Versioned<byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> removeAll(Collection<? extends Versioned<byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Cancellable> listen(CollectionEventListener<Versioned<byte[]>> listener, Executor executor) {
            return DefaultAsyncAtomicMap.this.listen(event -> {
                switch (event.type()) {
                    case INSERT:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.ADD, event.newValue()));
                        break;
                    case REMOVE:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.REMOVE, event.oldValue()));
                        break;
                    default:
                        break;
                }
            });
        }

        @Override
        public AsyncIterator<Versioned<byte[]>> iterator() {
            return iterate(MapGrpc.MapStub::entries, EntriesRequest.newBuilder()
                .setId(id())
                .build(), response -> toVersioned(response.getEntry().getValue()));
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncAtomicMap.this.close();
        }
    }

    private class EntrySet implements AsyncDistributedSet<Map.Entry<String, Versioned<byte[]>>> {
        @Override
        public String name() {
            return DefaultAsyncAtomicMap.this.name();
        }

        @Override
        public CompletableFuture<Boolean> add(Map.Entry<String, Versioned<byte[]>> element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> add(Map.Entry<String, Versioned<byte[]>> element, Duration ttl) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(Map.Entry<String, Versioned<byte[]>> element) {
            if (element.getValue().version() > 0) {
                return DefaultAsyncAtomicMap.this.remove(element.getKey(), element.getValue().version());
            } else {
                return DefaultAsyncAtomicMap.this.remove(element.getKey(), element.getValue().value());
            }
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncAtomicMap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncAtomicMap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncAtomicMap.this.clear();
        }

        @Override
        public CompletableFuture<Boolean> contains(Map.Entry<String, Versioned<byte[]>> element) {
            return get(element.getKey()).thenApply(versioned -> {
                if (versioned == null) {
                    return false;
                } else if (!Arrays.equals(versioned.value(), element.getValue().value())) {
                    return false;
                } else if (element.getValue().version() > 0 && versioned.version() != element.getValue().version()) {
                    return false;
                }
                return true;
            });
        }

        @Override
        public CompletableFuture<Boolean> addAll(Collection<? extends Map.Entry<String, Versioned<byte[]>>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> containsAll(Collection<? extends Map.Entry<String, Versioned<byte[]>>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> retainAll(Collection<? extends Map.Entry<String, Versioned<byte[]>>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> removeAll(Collection<? extends Map.Entry<String, Versioned<byte[]>>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Cancellable> listen(CollectionEventListener<Map.Entry<String, Versioned<byte[]>>> listener, Executor executor) {
            return DefaultAsyncAtomicMap.this.listen(event -> {
                switch (event.type()) {
                    case INSERT:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.ADD, Maps.immutableEntry(event.key(), event.newValue())));
                        break;
                    case REMOVE:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.REMOVE, Maps.immutableEntry(event.key(), event.oldValue())));
                        break;
                    default:
                        break;
                }
            });
        }

        @Override
        public AsyncIterator<Map.Entry<String, Versioned<byte[]>>> iterator() {
            return iterate(MapGrpc.MapStub::entries, EntriesRequest.newBuilder()
                .setId(id())
                .build(), response -> Maps.immutableEntry(
                response.getEntry().getKey(),
                toVersioned(response.getEntry().getValue())));
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncAtomicMap.this.close();
        }
    }
}
