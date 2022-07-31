// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import io.atomix.api.runtime.atomic.map.v1.*;
import io.atomix.client.Cancellable;
import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.collection.CollectionEvent;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.collection.impl.BlockingDistributedCollection;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.map.AsyncAtomicMap;
import io.atomix.client.map.AtomicMap;
import io.atomix.client.map.AtomicMapEvent;
import io.atomix.client.map.AtomicMapEventListener;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.DistributedSet;
import io.atomix.client.set.impl.BlockingDistributedSet;
import io.atomix.client.time.Versioned;
import io.grpc.Channel;
import io.grpc.Status;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicMap
        extends AbstractAsyncPrimitive<AsyncAtomicMap<String, byte[]>>
        implements AsyncAtomicMap<String, byte[]> {
    private final AtomicMapGrpc.AtomicMapStub stub;

    public DefaultAsyncAtomicMap(String name, Channel channel) {
        super(name);
        this.stub = AtomicMapGrpc.newStub(channel);
    }

    @Override
    protected CompletableFuture<AsyncAtomicMap<String, byte[]>> create(Map<String, String> tags) {
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
    public CompletableFuture<Integer> size() {
        return execute(stub::size, SizeRequest.newBuilder()
                .setId(id())
                .build())
                .thenApply(SizeResponse::getSize);
    }

    @Override
    public CompletableFuture<Boolean> containsKey(String key) {
        return execute(stub::get, GetRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .build())
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
        return execute(stub::get, GetRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .build())
                .thenApply(response -> toVersioned(response.getEntry()))
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
    public CompletableFuture<Versioned<byte[]>> computeIf(String key,
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

            if (r1 == null) {
                return putIfAbsent(key, computedValue).thenApply(version -> {
                    if (version.isPresent()) {
                        return new Versioned<>(computedValue, version.getAsLong());
                    }
                    return null;
                });
            } else if (computedValue == null) {
                return execute(stub::remove, RemoveRequest.newBuilder()
                        .setId(id())
                        .setKey(key)
                        .setVersion(r1.version())
                        .build())
                        .thenApply(response -> new Versioned<>(existingValue, r1.version()));
            } else {
                return execute(stub::update, UpdateRequest.newBuilder()
                        .setId(id())
                        .setKey(key)
                        .setValue(Value.newBuilder()
                                .setValue(ByteString.copyFrom(computedValue))
                                .build())
                        .setVersion(r1.version())
                        .build())
                        .thenApply(response -> new Versioned<>(computedValue, response.getVersion()));
            }
        });
    }

    @Override
    public CompletableFuture<Long> put(String key, byte[] value, Duration ttl) {
        return execute(stub::put, PutRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .setValue(Value.newBuilder()
                        .setValue(ByteString.copyFrom(value))
                        .setTtl(com.google.protobuf.Duration.newBuilder()
                                .setSeconds(ttl.getSeconds())
                                .setNanos(ttl.getNano())
                                .build())
                        .build())
                .build())
                .thenApply(PutResponse::getVersion);
    }

    @Override
    public CompletableFuture<Boolean> remove(String key) {
        return execute(stub::remove, RemoveRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .build())
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
    public CompletableFuture<Void> clear() {
        return execute(stub::clear, ClearRequest.newBuilder()
                .setId(id())
                .build())
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
    public CompletableFuture<OptionalLong> putIfAbsent(String key, byte[] value, Duration ttl) {
        return execute(stub::insert, InsertRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .setValue(Value.newBuilder()
                        .setValue(ByteString.copyFrom(value))
                        .setTtl(com.google.protobuf.Duration.newBuilder()
                                .setSeconds(ttl.getSeconds())
                                .setNanos(ttl.getNano())
                                .build())
                        .build())
                .build())
                .thenApply(response -> OptionalLong.of(response.getVersion()))
                .exceptionally(t -> {
                    if (Status.fromThrowable(t).getCode() == Status.Code.ALREADY_EXISTS) {
                        return OptionalLong.empty();
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
        return execute(stub::remove, RemoveRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .setVersion(version)
                .build())
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
    public CompletableFuture<OptionalLong> replace(String key, byte[] value) {
        return execute(stub::update, UpdateRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .setValue(Value.newBuilder()
                        .setValue(ByteString.copyFrom(value))
                        .build())
                .build())
                .thenApply(response -> OptionalLong.of(response.getVersion()))
                .exceptionally(t -> {
                    if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                        return OptionalLong.empty();
                    } else {
                        throw (RuntimeException) t;
                    }
                });
    }

    @Override
    public CompletableFuture<OptionalLong> replace(String key, byte[] oldValue, byte[] newValue) {
        return get(key).thenCompose(versioned -> {
            if (!Arrays.equals(versioned.value(), oldValue)) {
                return CompletableFuture.completedFuture(OptionalLong.empty());
            }
            return replace(key, versioned.version(), newValue);
        });
    }

    @Override
    public CompletableFuture<OptionalLong> replace(String key, long oldVersion, byte[] newValue) {
        return execute(stub::update, UpdateRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .setValue(Value.newBuilder()
                        .setValue(ByteString.copyFrom(newValue))
                        .build())
                .setVersion(oldVersion)
                .build())
                .thenApply(response -> OptionalLong.of(response.getVersion()))
                .exceptionally(t -> {
                    if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                        return OptionalLong.empty();
                    } else {
                        throw (RuntimeException) t;
                    }
                });
    }

    @Override
    public CompletableFuture<Long> lock(String key) {
        return null;
    }

    @Override
    public CompletableFuture<OptionalLong> tryLock(String key) {
        return null;
    }

    @Override
    public CompletableFuture<OptionalLong> tryLock(String key, Duration timeout) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> isLocked(String key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> isLocked(String key, long version) {
        return null;
    }

    @Override
    public CompletableFuture<Void> unlock(String key) {
        return null;
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicMapEventListener<String, byte[]> listener, Executor executor) {
        return execute(stub::events, EventsRequest.newBuilder()
                .setId(id())
                .build(), response -> {
            switch (response.getEvent().getType()) {
                case INSERT:
                    listener.event(new AtomicMapEvent<>(
                            AtomicMapEvent.Type.INSERT,
                            response.getEvent().getEntry().getKey(),
                            toVersioned(response.getEvent().getEntry()),
                            null));
                    break;
                case UPDATE:
                    listener.event(new AtomicMapEvent<>(
                            AtomicMapEvent.Type.UPDATE,
                            response.getEvent().getEntry().getKey(),
                            toVersioned(response.getEvent().getEntry()),
                            null));
                    break;
                case REMOVE:
                    listener.event(new AtomicMapEvent<>(
                            AtomicMapEvent.Type.REMOVE,
                            response.getEvent().getEntry().getKey(),
                            null,
                            toVersioned(response.getEvent().getEntry())));
                    break;
                default:
                    listener.event(new AtomicMapEvent<>(
                            AtomicMapEvent.Type.REPLAY,
                            response.getEvent().getEntry().getKey(),
                            toVersioned(response.getEvent().getEntry()),
                            null));
                    break;
            }
        }, executor);
    }

    @Override
    public AtomicMap<String, byte[]> sync(Duration operationTimeout) {
        return new BlockingAtomicMap<>(this, operationTimeout.toMillis());
    }

    private static Versioned<byte[]> toVersioned(Entry entry) {
        return new Versioned<>(
                entry.getValue().getValue().toByteArray(),
                entry.getVersion());
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
                    case REPLAY:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.REPLAY, event.key()));
                    default:
                        break;
                }
            });
        }

        @Override
        public AsyncIterator<String> iterator() {
            return iterate(stub::entries, EntriesRequest.newBuilder()
                    .setId(id())
                    .build(), response -> response.getEntry().getKey());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncAtomicMap.this.close();
        }

        @Override
        public DistributedSet<String> sync(Duration operationTimeout) {
            return new BlockingDistributedSet<>(this, operationTimeout.toMillis());
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
                    case REPLAY:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.REPLAY, event.newValue()));
                    default:
                        break;
                }
            });
        }

        @Override
        public AsyncIterator<Versioned<byte[]>> iterator() {
            return iterate(stub::entries, EntriesRequest.newBuilder()
                    .setId(id())
                    .build(), response -> toVersioned(response.getEntry()));
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncAtomicMap.this.close();
        }

        @Override
        public DistributedCollection<Versioned<byte[]>> sync(Duration operationTimeout) {
            return new BlockingDistributedCollection<>(this, operationTimeout.toMillis());
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
                    case REPLAY:
                        listener.event(new CollectionEvent<>(CollectionEvent.Type.REPLAY, Maps.immutableEntry(event.key(), event.newValue())));
                    default:
                        break;
                }
            });
        }

        @Override
        public AsyncIterator<Map.Entry<String, Versioned<byte[]>>> iterator() {
            return iterate(stub::entries, EntriesRequest.newBuilder()
                    .setId(id())
                    .build(), response -> Maps.immutableEntry(
                    response.getEntry().getKey(),
                    toVersioned(response.getEntry())));
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncAtomicMap.this.close();
        }

        @Override
        public DistributedSet<Map.Entry<String, Versioned<byte[]>>> sync(Duration operationTimeout) {
            return new BlockingDistributedSet<>(this, operationTimeout.toMillis());
        }
    }
}
