// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.Cancellable;
import io.atomix.map.AsyncAtomicMap;
import io.atomix.map.AtomicMapEventListener;
import io.atomix.time.Versioned;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * {@code AsyncConsistentMap} that caches entries on read.
 * <p>
 * The cache entries are automatically invalidated when updates are detected either locally or
 * remotely.
 * <p> This implementation only attempts to serve cached entries for {@link AsyncAtomicMap#get get}
 * {@link AsyncAtomicMap#getOrDefault(Object, Object) getOrDefault}, and
 * {@link AsyncAtomicMap#containsKey(Object) containsKey} calls. All other calls skip the cache
 * and directly go the backing map.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class CachingAsyncAtomicMap<K, V> extends DelegatingAsyncAtomicMap<K, V> {
    private final LoadingCache<K, CompletableFuture<Versioned<V>>> cache;
    private final AsyncAtomicMap<K, V> backingMap;
    private final AtomicMapEventListener<K, V> cacheUpdater;
    private final Map<AtomicMapEventListener<K, V>, Executor> mapEventListeners = new ConcurrentHashMap<>();

    /**
     * Constructor to configure cache size.
     *
     * @param backingMap  a distributed, strongly consistent map for backing
     * @param size the size of the backing cache
     */
    public CachingAsyncAtomicMap(AsyncAtomicMap<K, V> backingMap, long size) {
        super(backingMap);
        this.backingMap = backingMap;
        cache = CacheBuilder.newBuilder()
                .build(CacheLoader.from(CachingAsyncAtomicMap.super::get));
        cacheUpdater = event -> {
            Versioned<V> newValue = event.newValue();
            if (newValue == null) {
                cache.invalidate(event.key());
            } else {
                cache.put(event.key(), CompletableFuture.completedFuture(newValue));
            }
            mapEventListeners.forEach((listener, executor) -> executor.execute(() -> listener.event(event)));
        };
        super.listen(cacheUpdater, MoreExecutors.directExecutor());
    }

    @Override
    public CompletableFuture<Versioned<V>> get(K key) {
        return cache.getUnchecked(key)
                .whenComplete((r, e) -> {
                    if (e != null) {
                        cache.invalidate(key);
                    }
                });
    }

    @Override
    public CompletableFuture<Versioned<V>> getOrDefault(K key, V defaultValue) {
        return cache.getUnchecked(key).thenCompose(r -> {
            if (r == null) {
                CompletableFuture<Versioned<V>> versioned = backingMap.getOrDefault(key, defaultValue);
                cache.put(key, versioned);
                return versioned;
            } else {
                return CompletableFuture.completedFuture(r);
            }
        }).whenComplete((r, e) -> {
            if (e != null) {
                cache.invalidate(key);
            }
        });
    }

    @Override
    public CompletableFuture<Versioned<V>> computeIf(K key,
                                                     Predicate<? super V> condition,
                                                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.computeIf(key, condition, remappingFunction)
                .whenComplete((r, e) -> cache.invalidate(key));
    }

    @Override
    public CompletableFuture<Versioned<V>> put(K key, V value) {
        return super.put(key, value)
                .whenComplete((r, e) -> cache.invalidate(key));
    }

    @Override
    public CompletableFuture<Versioned<V>> putAndGet(K key, V value) {
        return super.putAndGet(key, value)
                .whenComplete((r, e) -> cache.invalidate(key));
    }

    @Override
    public CompletableFuture<Versioned<V>> putIfAbsent(K key, V value) {
        return super.putIfAbsent(key, value)
                .whenComplete((r, e) -> cache.invalidate(key));
    }

    @Override
    public CompletableFuture<Versioned<V>> remove(K key) {
        return super.remove(key)
                .whenComplete((r, e) -> cache.invalidate(key));
    }

    @Override
    public CompletableFuture<Boolean> containsKey(K key) {
        return cache.getUnchecked(key).thenApply(Objects::nonNull)
                .whenComplete((r, e) -> {
                    if (e != null) {
                        cache.invalidate(key);
                    }
                });
    }

    @Override
    public CompletableFuture<Void> clear() {
        return super.clear()
                .whenComplete((r, e) -> cache.invalidateAll());
    }

    @Override
    public CompletableFuture<Boolean> remove(K key, V value) {
        return super.remove(key, value)
                .whenComplete((r, e) -> {
                    if (r) {
                        cache.invalidate(key);
                    }
                });
    }

    @Override
    public CompletableFuture<Boolean> remove(K key, long version) {
        return super.remove(key, version)
                .whenComplete((r, e) -> {
                    if (r) {
                        cache.invalidate(key);
                    }
                });
    }

    @Override
    public CompletableFuture<Versioned<V>> replace(K key, V value) {
        return super.replace(key, value)
                .whenComplete((r, e) -> cache.invalidate(key));
    }

    @Override
    public CompletableFuture<Boolean> replace(K key, V oldValue, V newValue) {
        return super.replace(key, oldValue, newValue)
                .whenComplete((r, e) -> {
                    if (r) {
                        cache.invalidate(key);
                    }
                });
    }

    @Override
    public CompletableFuture<Boolean> replace(K key, long oldVersion, V newValue) {
        return super.replace(key, oldVersion, newValue)
                .whenComplete((r, e) -> {
                    if (r) {
                        cache.invalidate(key);
                    }
                });
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicMapEventListener<K, V> listener, Executor executor) {
        mapEventListeners.put(listener, executor);
        return CompletableFuture.completedFuture(() -> mapEventListeners.remove(listener));
    }
}
