// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import io.atomix.client.Cancellable;
import io.atomix.api.runtime.multimap.v1.ClearRequest;
import io.atomix.api.runtime.multimap.v1.CloseRequest;
import io.atomix.api.runtime.multimap.v1.ContainsRequest;
import io.atomix.api.runtime.multimap.v1.ContainsResponse;
import io.atomix.api.runtime.multimap.v1.CreateRequest;
import io.atomix.api.runtime.multimap.v1.EntriesRequest;
import io.atomix.api.runtime.multimap.v1.EventsRequest;
import io.atomix.api.runtime.multimap.v1.GetRequest;
import io.atomix.api.runtime.multimap.v1.GetResponse;
import io.atomix.api.runtime.multimap.v1.MultiMapGrpc;
import io.atomix.api.runtime.multimap.v1.PutAllRequest;
import io.atomix.api.runtime.multimap.v1.PutAllResponse;
import io.atomix.api.runtime.multimap.v1.PutRequest;
import io.atomix.api.runtime.multimap.v1.RemoveAllRequest;
import io.atomix.api.runtime.multimap.v1.RemoveAllResponse;
import io.atomix.api.runtime.multimap.v1.RemoveRequest;
import io.atomix.api.runtime.multimap.v1.ReplaceRequest;
import io.atomix.api.runtime.multimap.v1.ReplaceResponse;
import io.atomix.api.runtime.multimap.v1.SizeRequest;
import io.atomix.api.runtime.multimap.v1.SizeResponse;
import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.collection.CollectionEvent;
import io.atomix.client.collection.CollectionEventListener;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.iterator.AsyncIterator;
import io.atomix.client.map.AsyncDistributedMap;
import io.atomix.client.map.AsyncDistributedMultimap;
import io.atomix.client.map.DistributedMultimap;
import io.atomix.client.map.MapEventListener;
import io.atomix.client.map.MultimapEvent;
import io.atomix.client.map.MultimapEventListener;
import io.atomix.client.set.AsyncDistributedMultiset;
import io.atomix.client.set.AsyncDistributedSet;
import io.grpc.Status;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Atomix multimap implementation.
 */
public class DefaultAsyncDistributedMultimap
    extends AbstractAsyncPrimitive<AsyncDistributedMultimap<String, String>, DistributedMultimap<String, String>, MultiMapGrpc.MultiMapStub>
    implements AsyncDistributedMultimap<String, String> {

    public DefaultAsyncDistributedMultimap(String name, MultiMapGrpc.MultiMapStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    protected CompletableFuture<AsyncDistributedMultimap<String, String>> create(Map<String, String> tags) {
        return retry(MultiMapGrpc.MultiMapStub::create, CreateRequest.newBuilder()
            .setId(id())
            .putAllTags(tags)
            .build())
            .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return retry(MultiMapGrpc.MultiMapStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return retry(MultiMapGrpc.MultiMapStub::size, SizeRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(SizeResponse::getSize);
    }

    @Override
    public CompletableFuture<Boolean> containsKey(String key) {
        return retry(MultiMapGrpc.MultiMapStub::get, GetRequest.newBuilder()
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
    public CompletableFuture<Boolean> containsValue(String value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Void> clear() {
        return retry(MultiMapGrpc.MultiMapStub::clear, ClearRequest.newBuilder()
            .setId(id())
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> null);
    }

    @Override
    public AsyncDistributedSet<String> keySet() {
        return new KeySet();
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        return size().thenApply(s -> s == 0);
    }

    @Override
    public CompletableFuture<Boolean> containsEntry(String key, String value) {
        return retry(MultiMapGrpc.MultiMapStub::contains, ContainsRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(ContainsResponse::getResult);
    }

    @Override
    public CompletableFuture<Boolean> put(String key, String value) {
        return retry(MultiMapGrpc.MultiMapStub::put, PutRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(value)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ALREADY_EXISTS) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> remove(String key, String value) {
        return retry(MultiMapGrpc.MultiMapStub::remove, RemoveRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .setValue(value)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(response -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return null;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Boolean> removeAll(String key, Collection<? extends String> values) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Collection<String>> removeAll(String key) {
        return retry(MultiMapGrpc.MultiMapStub::removeAll, RemoveAllRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(RemoveAllResponse::getValuesList);
    }

    @Override
    public CompletableFuture<Boolean> putAll(String key, Collection<? extends String> values) {
        return retry(MultiMapGrpc.MultiMapStub::putAll, PutAllRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .addAllValues(new HashSet<>(values))
            .build(), DEFAULT_TIMEOUT)
            .thenApply(PutAllResponse::getUpdated);
    }

    @Override
    public CompletableFuture<Collection<String>> replaceValues(String key, Collection<String> values) {
        return retry(MultiMapGrpc.MultiMapStub::replace, ReplaceRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .addAllValues(new HashSet<>(values))
            .build(), DEFAULT_TIMEOUT)
            .thenApply(ReplaceResponse::getPrevValuesList);
    }

    @Override
    public CompletableFuture<Collection<String>> get(String key) {
        return retry(MultiMapGrpc.MultiMapStub::get, GetRequest.newBuilder()
            .setId(id())
            .setKey(key)
            .build(), DEFAULT_TIMEOUT)
            .thenApply(GetResponse::getValuesList);
    }

    @Override
    public AsyncDistributedMultiset<String> keys() {
        return new Keys();
    }

    @Override
    public AsyncDistributedMultiset<String> values() {
        return new Values();
    }

    @Override
    public AsyncDistributedCollection<Map.Entry<String, String>> entries() {
        return new Entries();
    }

    @Override
    public AsyncDistributedMap<String, Collection<String>> asMap() {
        return new EntryMap();
    }

    @Override
    public CompletableFuture<Cancellable> listen(MultimapEventListener<String, String> listener, Executor executor) {
        return execute(MultiMapGrpc.MultiMapStub::events, EventsRequest.newBuilder()
            .setId(id())
            .build(), response -> {
            switch (response.getEvent().getEventCase()) {
                case ADDED:
                    listener.event(new MultimapEvent<>(
                        MultimapEvent.Type.INSERT,
                        response.getEvent().getKey(),
                        response.getEvent().getAdded().getValue(),
                        null));
                    break;
                case REMOVED:
                    listener.event(new MultimapEvent<>(
                        MultimapEvent.Type.REMOVE,
                        response.getEvent().getKey(),
                        null,
                        response.getEvent().getRemoved().getValue()));
                    break;
            }
        }, executor);
    }

    private class KeySet implements AsyncDistributedSet<String> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMultimap.this.name();
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
            return DefaultAsyncDistributedMultimap.this.removeAll(element)
                .thenApply(values -> !values.isEmpty());
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMultimap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMultimap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMultimap.this.clear();
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
            return DefaultAsyncDistributedMultimap.this.listen(event -> {
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
            return iterate(MultiMapGrpc.MultiMapStub::entries, EntriesRequest.newBuilder()
                .setId(id())
                .build(), response -> response.getEntry().getKey());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMultimap.this.close();
        }
    }

    private class Keys implements AsyncDistributedMultiset<String> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMultimap.this.name();
        }

        @Override
        public CompletableFuture<Integer> count(Object element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> add(String element, int occurrences) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> remove(Object element, int occurrences) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> setCount(String element, int count) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> setCount(String element, int oldCount, int newCount) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public AsyncDistributedSet<String> elementSet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncDistributedSet<Multiset.Entry<String>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Boolean> add(String element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(String element) {
            return DefaultAsyncDistributedMultimap.this.removeAll(element)
                .thenApply(values -> !values.isEmpty());
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMultimap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMultimap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMultimap.this.clear();
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
            return DefaultAsyncDistributedMultimap.this.listen(event -> {
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
            return iterate(MultiMapGrpc.MultiMapStub::entries, EntriesRequest.newBuilder()
                .setId(id())
                .build(), response -> response.getEntry().getKey());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMultimap.this.close();
        }
    }

    private class Values implements AsyncDistributedMultiset<String> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMultimap.this.name();
        }

        @Override
        public CompletableFuture<Integer> count(Object element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> add(String element, int occurrences) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> remove(Object element, int occurrences) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> setCount(String element, int count) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> setCount(String element, int oldCount, int newCount) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public AsyncDistributedSet<String> elementSet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncDistributedSet<Multiset.Entry<String>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Boolean> add(String element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(String element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMultimap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMultimap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMultimap.this.clear();
        }

        @Override
        public CompletableFuture<Boolean> contains(String element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
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
            return DefaultAsyncDistributedMultimap.this.listen(event -> {
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
        public AsyncIterator<String> iterator() {
            return iterate(MultiMapGrpc.MultiMapStub::entries, EntriesRequest.newBuilder()
                .setId(id())
                .build(), response -> response.getEntry().getValue());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMultimap.this.close();
        }
    }

    private class Entries implements AsyncDistributedSet<Map.Entry<String, String>> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMultimap.this.name();
        }

        @Override
        public CompletableFuture<Boolean> add(Map.Entry<String, String> element) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> add(Map.Entry<String, String> element, Duration ttl) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(Map.Entry<String, String> element) {
            return DefaultAsyncDistributedMultimap.this.remove(element.getKey(), element.getValue());
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMultimap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMultimap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMultimap.this.clear();
        }

        @Override
        public CompletableFuture<Boolean> contains(Map.Entry<String, String> entry) {
            return containsEntry(entry.getKey(), entry.getValue());
        }

        @Override
        public CompletableFuture<Boolean> addAll(Collection<? extends Map.Entry<String, String>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> containsAll(Collection<? extends Map.Entry<String, String>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> retainAll(Collection<? extends Map.Entry<String, String>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> removeAll(Collection<? extends Map.Entry<String, String>> c) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Cancellable> listen(CollectionEventListener<Map.Entry<String, String>> listener, Executor executor) {
            return DefaultAsyncDistributedMultimap.this.listen(event -> {
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
        public AsyncIterator<Map.Entry<String, String>> iterator() {
            return iterate(MultiMapGrpc.MultiMapStub::entries, EntriesRequest.newBuilder()
                .setId(id())
                .build(), response -> Maps.immutableEntry(
                response.getEntry().getKey(),
                response.getEntry().getValue()));
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMultimap.this.close();
        }
    }

    private class EntryMap implements AsyncDistributedMap<String, Collection<String>> {
        @Override
        public String name() {
            return DefaultAsyncDistributedMultimap.this.name();
        }

        @Override
        public CompletableFuture<Integer> size() {
            return DefaultAsyncDistributedMultimap.this.size();
        }

        @Override
        public CompletableFuture<Boolean> isEmpty() {
            return DefaultAsyncDistributedMultimap.this.isEmpty();
        }

        @Override
        public CompletableFuture<Boolean> containsKey(String key) {
            return DefaultAsyncDistributedMultimap.this.containsKey(key);
        }

        @Override
        public CompletableFuture<Boolean> containsValue(Collection<String> value) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Collection<String>> get(String key) {
            return DefaultAsyncDistributedMultimap.this.get(key);
        }

        @Override
        public CompletableFuture<Collection<String>> put(String key, Collection<String> value) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Collection<String>> remove(String key) {
            return DefaultAsyncDistributedMultimap.this.removeAll(key);
        }

        @Override
        public CompletableFuture<Void> putAll(java.util.Map<? extends String, ? extends Collection<String>> m) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Void> clear() {
            return DefaultAsyncDistributedMultimap.this.clear();
        }

        @Override
        public AsyncDistributedSet<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncDistributedCollection<Collection<String>> values() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncDistributedSet<java.util.Map.Entry<String, Collection<String>>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Collection<String>> getOrDefault(String key, Collection<String> defaultValue) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Collection<String>> putIfAbsent(String key, Collection<String> value) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> remove(String key, Collection<String> value) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Boolean> replace(String key, Collection<String> oldValue, Collection<String> newValue) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Collection<String>> replace(String key, Collection<String> value) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Collection<String>> computeIfAbsent(String key, Function<? super String, ? extends Collection<String>> mappingFunction) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Collection<String>> computeIfPresent(String key, BiFunction<? super String, ? super Collection<String>, ? extends Collection<String>> remappingFunction) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Collection<String>> compute(String key, BiFunction<? super String, ? super Collection<String>, ? extends Collection<String>> remappingFunction) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Cancellable> listen(MapEventListener<String, Collection<String>> listener, Executor executor) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public CompletableFuture<Void> close() {
            return DefaultAsyncDistributedMultimap.this.close();
        }
    }
}
