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
import io.atomix.client.map.AtomicMap;
import io.atomix.client.map.AtomicMapEvent;
import io.atomix.client.map.AtomicMapEventListener;
import io.atomix.client.map.DistributedMap;
import io.atomix.client.map.MapEvent;
import io.atomix.client.map.MapEventListener;
import io.atomix.client.set.AsyncDistributedSet;
import io.atomix.client.set.impl.TranscodingAsyncDistributedSet;
import io.atomix.client.time.Versioned;

import java.time.Duration;
import java.util.Map;
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
public class TranscodingAsyncDistributedMap<K1, V1, K2, V2> extends DelegatingAsyncPrimitive implements AsyncDistributedMap<K1, V1> {

    private final AsyncDistributedMap<K2, V2> backingMap;
    protected final Function<K1, K2> keyEncoder;
    protected final Function<K2, K1> keyDecoder;
    protected final Function<V2, V1> valueDecoder;
    protected final Function<V1, V2> valueEncoder;
    protected final Function<Entry<K2, V2>, Entry<K1, V1>> entryDecoder;
    protected final Function<Entry<K1, V1>, Entry<K2, V2>> entryEncoder;

    public TranscodingAsyncDistributedMap(
        AsyncDistributedMap<K2, V2> backingMap,
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
    public CompletableFuture<Boolean> isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public CompletableFuture<Void> putAll(Map<? extends K1, ? extends V1> m) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<V1> computeIfAbsent(K1 key, Function<? super K1, ? extends V1> mappingFunction) {
        try {
            return backingMap.computeIfAbsent(keyEncoder.apply(key),
                    k -> valueEncoder.apply(mappingFunction.apply(keyDecoder.apply(k))))
                .thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<V1> computeIfPresent(K1 key, BiFunction<? super K1, ? super V1, ? extends V1> remappingFunction) {
        try {
            return backingMap.computeIfPresent(keyEncoder.apply(key),
                    (k, v) -> valueEncoder.apply(remappingFunction.apply(keyDecoder.apply(k),
                        valueDecoder.apply(v))))
                .thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<V1> compute(K1 key, BiFunction<? super K1, ? super V1, ? extends V1> remappingFunction) {
        try {
            return backingMap.compute(keyEncoder.apply(key),
                    (k, v) -> valueEncoder.apply(remappingFunction.apply(keyDecoder.apply(k),
                        valueDecoder.apply(v))))
                .thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
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
    public CompletableFuture<V1> get(K1 key) {
        try {
            return backingMap.get(keyEncoder.apply(key)).thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<V1> getOrDefault(K1 key, V1 defaultValue) {
        try {
            return backingMap.getOrDefault(keyEncoder.apply(key), valueEncoder.apply(defaultValue))
                .thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<V1> put(K1 key, V1 value) {
        try {
            return backingMap.put(keyEncoder.apply(key), valueEncoder.apply(value))
                .thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<V1> remove(K1 key) {
        try {
            return backingMap.remove(keyEncoder.apply(key))
                .thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> clear() {
        return backingMap.clear();
    }

    @Override
    public AsyncDistributedSet<K1> keySet() {
        return new TranscodingAsyncDistributedSet<>(backingMap.keySet(), keyEncoder, keyDecoder);
    }

    @Override
    public AsyncDistributedCollection<V1> values() {
        return new TranscodingAsyncDistributedCollection<>(backingMap.values(), valueEncoder, valueDecoder);
    }

    @Override
    public AsyncDistributedSet<Entry<K1, V1>> entrySet() {
        return new TranscodingAsyncDistributedSet<>(backingMap.entrySet(), entryEncoder, entryDecoder);
    }

    @Override
    public CompletableFuture<V1> putIfAbsent(K1 key, V1 value) {
        try {
            return backingMap.putIfAbsent(keyEncoder.apply(key), valueEncoder.apply(value))
                .thenApply(valueDecoder);
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
    public CompletableFuture<V1> replace(K1 key, V1 value) {
        try {
            return backingMap.replace(keyEncoder.apply(key), valueEncoder.apply(value))
                .thenApply(valueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> replace(K1 key, V1 oldValue, V1 newValue) {
        try {
            return backingMap.replace(keyEncoder.apply(key),
                valueEncoder.apply(oldValue),
                valueEncoder.apply(newValue));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Cancellable> listen(MapEventListener<K1, V1> listener, Executor executor) {
        return backingMap.listen(event -> listener.event(new MapEvent<>(
            event.type(),
            keyDecoder.apply(event.key()),
            event.newValue() != null ? valueDecoder.apply(event.newValue()) : null,
            event.oldValue() != null ? valueDecoder.apply(event.oldValue()) : null)), executor);
    }

    @Override
    public DistributedMap<K1, V1> sync(Duration operationTimeout) {
        return new BlockingDistributedMap<>(this, operationTimeout.toMillis());
    }
}