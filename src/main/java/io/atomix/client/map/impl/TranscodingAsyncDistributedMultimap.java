// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map.impl;

import com.google.common.collect.Maps;
import io.atomix.client.Cancellable;
import io.atomix.client.DelegatingAsyncPrimitive;
import io.atomix.client.collection.AsyncDistributedCollection;
import io.atomix.client.collection.impl.TranscodingAsyncDistributedCollection;
import io.atomix.client.map.AsyncAtomicMap;
import io.atomix.client.map.AsyncDistributedMap;
import io.atomix.client.map.AsyncDistributedMultimap;
import io.atomix.client.map.AtomicMap;
import io.atomix.client.map.AtomicMapEvent;
import io.atomix.client.map.AtomicMapEventListener;
import io.atomix.client.map.DistributedMultimap;
import io.atomix.client.map.MultimapEvent;
import io.atomix.client.map.MultimapEventListener;
import io.atomix.client.set.AsyncDistributedMultiset;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.impl.TranscodingAsyncDistributedMultiset;
import io.atomix.client.set.impl.TranscodingAsyncDistributedSet;
import io.atomix.client.time.Versioned;

import java.time.Duration;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An {@code AsyncConsistentMap} that maps its operations to operations on a
 * differently typed {@code AsyncConsistentMap} by transcoding operation inputs and outputs.
 *
 * @param <K2> key type of other map
 * @param <V2> value type of other map
 * @param <K1> key type of this map
 * @param <V1> value type of this map
 */
public class TranscodingAsyncDistributedMultimap<K1, V1, K2, V2> extends DelegatingAsyncPrimitive implements AsyncDistributedMultimap<K1, V1> {

    private final AsyncDistributedMultimap<K2, V2> backingMap;
    protected final Function<K1, K2> keyEncoder;
    protected final Function<K2, K1> keyDecoder;
    protected final Function<V2, V1> valueDecoder;
    protected final Function<V1, V2> valueEncoder;
    protected final Function<Entry<K2, V2>, Entry<K1, V1>> entryDecoder;
    protected final Function<Entry<K1, V1>, Entry<K2, V2>> entryEncoder;

    public TranscodingAsyncDistributedMultimap(
        AsyncDistributedMultimap<K2, V2> backingMap,
        Function<K1, K2> keyEncoder,
        Function<K2, K1> keyDecoder,
        Function<V1, V2> valueEncoder,
        Function<V2, V1> valueDecoder) {
        super(backingMap);
        this.backingMap = backingMap;
        this.keyEncoder = k -> k == null ? null : keyEncoder.apply(k);
        this.keyDecoder = k -> k == null ? null : keyDecoder.apply(k);
        this.valueEncoder = v -> v == null ? null : valueEncoder.apply(v);
        this.valueDecoder = v -> v == null ? null : valueDecoder.apply(v);
        this.entryDecoder = e -> e == null ? null : Maps.immutableEntry(keyDecoder.apply(e.getKey()), valueDecoder.apply(e.getValue()));
        this.entryEncoder = e -> e == null ? null : Maps.immutableEntry(keyEncoder.apply(e.getKey()), valueEncoder.apply(e.getValue()));
    }

    @Override
    public CompletableFuture<Integer> size() {
        return backingMap.size();
    }

    @Override
    public CompletableFuture<Boolean> containsKey(K1 key) {
        try {
            return backingMap.containsKey(keyEncoder.apply(key));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> containsValue(V1 value) {
        try {
            return backingMap.containsValue(valueEncoder.apply(value));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        try {
            return backingMap.isEmpty();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> containsEntry(K1 key, V1 value) {
        try {
            return backingMap.containsEntry(keyEncoder.apply(key), valueEncoder.apply(value));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> put(K1 key, V1 value) {
        try {
            return backingMap.put(keyEncoder.apply(key), valueEncoder.apply(value));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> remove(K1 key, V1 value) {
        try {
            return backingMap.remove(keyEncoder.apply(key), valueEncoder.apply(value));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> removeAll(K1 key, Collection<? extends V1> values) {
        try {
            return backingMap.removeAll(keyEncoder.apply(key),
                values.stream().map(valueEncoder::apply).collect(Collectors.toSet()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Collection<V1>> removeAll(K1 key) {
        try {
            return backingMap.removeAll(keyEncoder.apply(key))
                .thenApply(results -> results.stream().map(valueDecoder).collect(Collectors.toSet()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> putAll(K1 key, Collection<? extends V1> values) {
        try {
            return backingMap.putAll(keyEncoder.apply(key),
                values.stream().map(valueEncoder).collect(Collectors.toSet()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Collection<V1>> replaceValues(K1 key, Collection<V1> values) {
        try {
            return backingMap.replaceValues(keyEncoder.apply(key),
                    values.stream().map(valueEncoder).collect(Collectors.toSet()))
                .thenApply(results -> results.stream().map(valueDecoder).collect(Collectors.toSet()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> clear() {
        try {
            return backingMap.clear();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Collection<V1>> get(K1 key) {
        try {
            return backingMap.get(keyEncoder.apply(key))
                .thenApply(results -> results.stream().map(valueDecoder).collect(Collectors.toSet()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public AsyncDistributedSet<K1> keySet() {
        return new TranscodingAsyncDistributedSet<>(backingMap.keySet(), keyEncoder, keyDecoder);
    }

    @Override
    public AsyncDistributedMultiset<K1> keys() {
        return new TranscodingAsyncDistributedMultiset<>(backingMap.keys(), keyEncoder, keyDecoder);
    }

    @Override
    public AsyncDistributedMultiset<V1> values() {
        return new TranscodingAsyncDistributedMultiset<>(backingMap.values(), valueEncoder, valueDecoder);
    }

    @Override
    public AsyncDistributedCollection<Entry<K1, V1>> entries() {
        return new TranscodingAsyncDistributedCollection<>(backingMap.entries(), entryEncoder, entryDecoder);
    }

    @Override
    public AsyncDistributedMap<K1, Collection<V1>> asMap() {
        return new TranscodingAsyncDistributedMap<>(
            backingMap.asMap(), keyEncoder, keyDecoder,
            values -> values.stream().map(valueEncoder).collect(Collectors.toSet()),
            values -> values.stream().map(valueDecoder).collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Cancellable> listen(MultimapEventListener<K1, V1> listener, Executor executor) {
        return backingMap.listen(event -> listener.event(new MultimapEvent<K1, V1>(
            event.type(),
            keyDecoder.apply(event.key()),
            valueDecoder.apply(event.newValue()),
            valueDecoder.apply(event.oldValue()))), executor);
    }

    @Override
    public DistributedMultimap<K1, V1> sync(Duration operationTimeout) {
        return new BlockingDistributedMultimap<>(this, operationTimeout.toMillis());
    }
}