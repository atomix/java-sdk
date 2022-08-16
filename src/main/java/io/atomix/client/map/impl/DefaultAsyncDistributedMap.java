// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import io.atomix.api.runtime.map.v1.*;
import io.atomix.api.runtime.map.v1.MapGrpc;
import io.atomix.client.Cancellable;
import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.collection.CollectionEvent;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.collection.impl.BlockingDistributedCollection;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.map.*;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncDistributedMap
        extends AbstractAsyncPrimitive<AsyncDistributedMap<String, byte[]>>
        implements AsyncDistributedMap<String, byte[]> {
    private final MapGrpc.MapStub stub;

    public DefaultAsyncDistributedMap(String name, Channel channel) {
        super(name);
        this.stub = MapGrpc.newStub(channel);
    }

    @Override
    protected CompletableFuture<AsyncDistributedMap<String, byte[]>> create(Map<String, String> tags) {
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
        return size().thenApply(size -> size > 0);
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
    public CompletableFuture<byte[]> get(String key) {
        return execute(stub::get, GetRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .build())
                .thenApply(response -> response.getValue().getValue().toByteArray())
                .exceptionally(t -> {
                    if (Status.fromThrowable(t).getCode() == Status.NOT_FOUND.getCode()) {
                        return null;
                    } else {
                        throw (RuntimeException) t;
                    }
                });
    }

    @Override
    public CompletableFuture<byte[]> getOrDefault(String key, byte[] defaultValue) {
        return get(key).thenApply(value -> {
            if (value == null) {
                return defaultValue;
            }
            return value;
        });
    }

    @Override
    public CompletableFuture<byte[]> put(String key, byte[] value) {
        return execute(stub::put, PutRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .setValue(Value.newBuilder()
                        .setValue(ByteString.copyFrom(value))
                        .build())
                .build())
                .thenApply(response -> response.getPrevValue().getValue().toByteArray());
    }

    @Override
    public CompletableFuture<Void> putAll(Map<? extends String, ? extends byte[]> m) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<byte[]> remove(String key) {
        return execute(stub::remove, RemoveRequest.newBuilder()
                .setId(id())
                .setKey(key)
                .build())
                .thenApply(response -> response.getValue().getValue().toByteArray())
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
    public AsyncDistributedCollection<byte[]> values() {
        return new Values();
    }

    @Override
    public AsyncDistributedSet<Map.Entry<String, byte[]>> entrySet() {
        return new EntrySet();
    }

    @Override
    public CompletableFuture<byte[]> putIfAbsent(String key, byte[] value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> remove(String key, byte[] value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<byte[]> replace(String key, byte[] value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Boolean> replace(String key, byte[] oldValue, byte[] newValue) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<byte[]> computeIfAbsent(String key, Function<? super String, ? extends byte[]> mappingFunction) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<byte[]> computeIfPresent(String key, BiFunction<? super String, ? super byte[], ? extends byte[]> remappingFunction) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<byte[]> compute(String key, BiFunction<? super String, ? super byte[], ? extends byte[]> remappingFunction) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Cancellable> listen(MapEventListener<String, byte[]> listener, Executor executor) {
        return execute(stub::events, EventsRequest.newBuilder()
                .setId(id())
                .build(), response -> {
            switch (response.getEvent().getEventCase()) {
                case INSERTED:
                    listener.event(new MapEvent<>(
                            MapEvent.Type.INSERT,
                            response.getEvent().getKey(),
                            response.getEvent().getInserted().getValue().getValue().toByteArray(),
                            null));
                    break;
                case UPDATED:
                    listener.event(new MapEvent<>(
                            MapEvent.Type.UPDATE,
                            response.getEvent().getKey(),
                            response.getEvent().getUpdated().getValue().getValue().toByteArray(),
                            response.getEvent().getUpdated().getPrevValue().getValue().toByteArray()));
                    break;
                case REMOVED:
                    listener.event(new MapEvent<>(
                            MapEvent.Type.REMOVE,
                            response.getEvent().getKey(),
                            null,
                            response.getEvent().getRemoved().getValue().getValue().toByteArray()));
                    break;
            }
        }, executor);
    }

    @Override
    public DistributedMap<String, byte[]> sync(Duration operationTimeout) {
        return new BlockingDistributedMap<>(this, operationTimeout.toMillis());
    }

    private class KeySet implements AsyncDistributedSet<String> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMap.this.name();
        }

        @Override
        public CompletableFuture<Boolean> add(String element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(String element) {
            return DefaultAsyncDistributedMap.this.remove(element)
                    .thenApply(versioned -> versioned != null);
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMap.this.clear();
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
            return DefaultAsyncDistributedMap.this.listen(event -> {
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
            return iterate(stub::entries, EntriesRequest.newBuilder()
                    .setId(id())
                    .build(), response -> response.getEntry().getKey());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMap.this.close();
        }

        @Override
        public DistributedSet<String> sync(Duration operationTimeout) {
            return new BlockingDistributedSet<>(this, operationTimeout.toMillis());
        }
    }

    private class Values implements AsyncDistributedCollection<byte[]> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMap.this.name();
        }

        @Override
        public CompletableFuture<Boolean> add(byte[] element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(byte[] element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMap.this.clear();
        }

        @Override
        public CompletableFuture<Boolean> contains(byte[] element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> addAll(Collection<? extends byte[]> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> containsAll(Collection<? extends byte[]> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> retainAll(Collection<? extends byte[]> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> removeAll(Collection<? extends byte[]> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Cancellable> listen(CollectionEventListener<byte[]> listener, Executor executor) {
            return DefaultAsyncDistributedMap.this.listen(event -> {
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
        public AsyncIterator<byte[]> iterator() {
            return iterate(stub::entries, EntriesRequest.newBuilder()
                    .setId(id())
                    .build(), response -> response.getEntry().getValue().getValue().toByteArray());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMap.this.close();
        }

        @Override
        public DistributedCollection<byte[]> sync(Duration operationTimeout) {
            return new BlockingDistributedCollection<>(this, operationTimeout.toMillis());
        }
    }

    private class EntrySet implements AsyncDistributedSet<Map.Entry<String, byte[]>> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMap.this.name();
        }

        @Override
        public CompletableFuture<Boolean> add(Map.Entry<String, byte[]> element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(Map.Entry<String, byte[]> element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMap.this.clear();
        }

        @Override
        public CompletableFuture<Boolean> contains(Map.Entry<String, byte[]> element) {
            return get(element.getKey()).thenApply(value -> {
                if (value == null) {
                    return element.getValue() == null;
                } else if (!Arrays.equals(value, element.getValue())) {
                    return false;
                }
                return true;
            });
        }

        @Override
        public CompletableFuture<Boolean> addAll(Collection<? extends Map.Entry<String, byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> containsAll(Collection<? extends Map.Entry<String, byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> retainAll(Collection<? extends Map.Entry<String, byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> removeAll(Collection<? extends Map.Entry<String, byte[]>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Cancellable> listen(CollectionEventListener<Map.Entry<String, byte[]>> listener, Executor executor) {
            return DefaultAsyncDistributedMap.this.listen(event -> {
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
        public AsyncIterator<Map.Entry<String, byte[]>> iterator() {
            return iterate(stub::entries, EntriesRequest.newBuilder()
                    .setId(id())
                    .build(), response -> Maps.immutableEntry(
                    response.getEntry().getKey(),
                    response.getEntry().getValue().getValue().toByteArray()));
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMap.this.close();
        }

        @Override
        public DistributedSet<Map.Entry<String, byte[]>> sync(Duration operationTimeout) {
            return new BlockingDistributedSet<>(this, operationTimeout.toMillis());
        }
    }
}
