// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import com.google.common.collect.Maps;
import io.atomix.Cancellable;
import io.atomix.DelegatingAsyncPrimitive;
import io.atomix.collection.AsyncDistributedCollection;
import io.atomix.collection.impl.TranscodingAsyncDistributedCollection;
import io.atomix.map.AsyncAtomicMap;
import io.atomix.map.AtomicMap;
import io.atomix.map.AtomicMapEvent;
import io.atomix.map.AtomicMapEventListener;
import io.atomix.set.AsyncDistributedSet;
import io.atomix.set.impl.TranscodingAsyncDistributedSet;
import io.atomix.time.Versioned;

import java.time.Duration;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An {@code AsyncConsistentMap} that maps its operations to operations on a
 * differently typed {@code AsyncConsistentMap} by transcoding operation inputs and outputs.
 *
 * @param <K2> key type of other map
 * @param <V2> value type of other map
 * @param <K1> key type of this map
 * @param <V1> value type of this map
 */
public class TranscodingAsyncAtomicMap<K1, V1, K2, V2>
    extends DelegatingAsyncPrimitive<AsyncAtomicMap<K1, V1>, AtomicMap<K1, V1>, AsyncAtomicMap<K2, V2>>
    implements AsyncAtomicMap<K1, V1> {

    private final AsyncAtomicMap<K2, V2> backingMap;
    protected final Function<K1, K2> keyEncoder;
    protected final Function<K2, K1> keyDecoder;
    protected final Function<V2, V1> valueDecoder;
    protected final Function<V1, V2> valueEncoder;
    protected final Function<Versioned<V2>, Versioned<V1>> versionedValueDecoder;
    protected final Function<Versioned<V1>, Versioned<V2>> versionedValueEncoder;
    protected final Function<Entry<K2, Versioned<V2>>, Entry<K1, Versioned<V1>>> entryDecoder;
    protected final Function<Entry<K1, Versioned<V1>>, Entry<K2, Versioned<V2>>> entryEncoder;

    public TranscodingAsyncAtomicMap(
        AsyncAtomicMap<K2, V2> backingMap,
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
        this.versionedValueDecoder = v -> v == null ? null : v.map(valueDecoder);
        this.versionedValueEncoder = v -> v == null ? null : v.map(valueEncoder);
        this.entryDecoder = e -> e == null ? null : Maps.immutableEntry(keyDecoder.apply(e.getKey()), versionedValueDecoder.apply(e.getValue()));
        this.entryEncoder = e -> e == null ? null : Maps.immutableEntry(keyEncoder.apply(e.getKey()), versionedValueEncoder.apply(e.getValue()));
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
    public CompletableFuture<Versioned<V1>> get(K1 key) {
        try {
            return backingMap.get(keyEncoder.apply(key)).thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> getOrDefault(K1 key, V1 defaultValue) {
        try {
            return backingMap.getOrDefault(keyEncoder.apply(key), valueEncoder.apply(defaultValue))
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> computeIf(K1 key,
                                                      Predicate<? super V1> condition,
                                                      BiFunction<? super K1, ? super V1, ? extends V1> remappingFunction) {
        try {
            return backingMap.computeIf(keyEncoder.apply(key),
                    v -> condition.test(valueDecoder.apply(v)),
                    (k, v) -> valueEncoder.apply(remappingFunction.apply(keyDecoder.apply(k),
                        valueDecoder.apply(v))))
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> put(K1 key, V1 value) {
        try {
            return backingMap.put(keyEncoder.apply(key), valueEncoder.apply(value))
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> put(K1 key, V1 value, Duration ttl) {
        try {
            return backingMap.put(keyEncoder.apply(key), valueEncoder.apply(value), ttl)
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> putAndGet(K1 key, V1 value) {
        try {
            return backingMap.putAndGet(keyEncoder.apply(key), valueEncoder.apply(value))
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> putAndGet(K1 key, V1 value, Duration ttl) {
        try {
            return backingMap.putAndGet(keyEncoder.apply(key), valueEncoder.apply(value), ttl)
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> remove(K1 key) {
        try {
            return backingMap.remove(keyEncoder.apply(key))
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> clear() {
        return backingMap.clear();
    }

    @Override
    public CompletableFuture<Void> lock(K1 key) {
        return backingMap.lock(keyEncoder.apply(key));
    }

    @Override
    public CompletableFuture<Boolean> tryLock(K1 key) {
        return backingMap.tryLock(keyEncoder.apply(key));
    }

    @Override
    public CompletableFuture<Boolean> tryLock(K1 key, Duration timeout) {
        return backingMap.tryLock(keyEncoder.apply(key), timeout);
    }

    @Override
    public CompletableFuture<Boolean> isLocked(K1 key) {
        return backingMap.isLocked(keyEncoder.apply(key));
    }

    @Override
    public CompletableFuture<Void> unlock(K1 key) {
        return backingMap.unlock(keyEncoder.apply(key));
    }

    @Override
    public AsyncDistributedSet<K1> keySet() {
        return new TranscodingAsyncDistributedSet<>(backingMap.keySet(), keyEncoder, keyDecoder);
    }

    @Override
    public AsyncDistributedCollection<Versioned<V1>> values() {
        return new TranscodingAsyncDistributedCollection<>(backingMap.values(), versionedValueEncoder, versionedValueDecoder);
    }

    @Override
    public AsyncDistributedSet<Entry<K1, Versioned<V1>>> entrySet() {
        return new TranscodingAsyncDistributedSet<>(backingMap.entrySet(), entryEncoder, entryDecoder);
    }

    @Override
    public CompletableFuture<Versioned<V1>> putIfAbsent(K1 key, V1 value) {
        try {
            return backingMap.putIfAbsent(keyEncoder.apply(key), valueEncoder.apply(value))
                .thenApply(versionedValueDecoder);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> putIfAbsent(K1 key, V1 value, Duration ttl) {
        try {
            return backingMap.putIfAbsent(keyEncoder.apply(key), valueEncoder.apply(value), ttl)
                .thenApply(versionedValueDecoder);
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
    public CompletableFuture<Boolean> remove(K1 key, long version) {
        try {
            return backingMap.remove(keyEncoder.apply(key), version);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Versioned<V1>> replace(K1 key, V1 value) {
        try {
            return backingMap.replace(keyEncoder.apply(key), valueEncoder.apply(value))
                .thenApply(versionedValueDecoder);
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
    public CompletableFuture<Boolean> replace(K1 key, long oldVersion, V1 newValue) {
        try {
            return backingMap.replace(keyEncoder.apply(key), oldVersion, valueEncoder.apply(newValue));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicMapEventListener<K1, V1> listener, Executor executor) {
        return backingMap.listen(event -> listener.event(new AtomicMapEvent<K1, V1>(
            event.type(),
            keyDecoder.apply(event.key()),
            event.newValue() != null ? event.newValue().map(valueDecoder) : null,
            event.oldValue() != null ? event.oldValue().map(valueDecoder) : null)), executor);
    }
}