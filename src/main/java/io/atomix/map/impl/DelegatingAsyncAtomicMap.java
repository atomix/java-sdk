// SPDX-FileCopyrightText: 2016-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import io.atomix.Cancellable;
import io.atomix.DelegatingAsyncPrimitive;
import io.atomix.collection.AsyncDistributedCollection;
import io.atomix.map.AsyncAtomicMap;
import io.atomix.map.AtomicMap;
import io.atomix.map.AtomicMapEventListener;
import io.atomix.set.AsyncDistributedSet;
import io.atomix.time.Versioned;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * {@code AsyncConsistentMap} that merely delegates control to
 * another AsyncConsistentMap.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class DelegatingAsyncAtomicMap<K, V>
        extends DelegatingAsyncPrimitive<AsyncAtomicMap<K, V>, AtomicMap<K, V>, AsyncAtomicMap<K, V>>
        implements AsyncAtomicMap<K, V> {

    DelegatingAsyncAtomicMap(AsyncAtomicMap<K, V> delegate) {
        super(delegate);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return delegate().size();
    }

    @Override
    public CompletableFuture<Boolean> containsKey(K key) {
        return delegate().containsKey(key);
    }

    @Override
    public CompletableFuture<Boolean> containsValue(V value) {
        return delegate().containsValue(value);
    }

    @Override
    public CompletableFuture<Versioned<V>> get(K key) {
        return delegate().get(key);
    }

    @Override
    public CompletableFuture<Versioned<V>> getOrDefault(K key, V defaultValue) {
        return delegate().getOrDefault(key, defaultValue);
    }

    @Override
    public CompletableFuture<Versioned<V>> computeIf(K key,
                                                     Predicate<? super V> condition,
                                                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate().computeIf(key, condition, remappingFunction);
    }

    @Override
    public CompletableFuture<Versioned<V>> put(K key, V value) {
        return delegate().put(key, value);
    }

    @Override
    public CompletableFuture<Versioned<V>> put(K key, V value, Duration ttl) {
        return delegate().put(key, value, ttl);
    }

    @Override
    public CompletableFuture<Versioned<V>> putAndGet(K key, V value) {
        return delegate().putAndGet(key, value);
    }

    @Override
    public CompletableFuture<Versioned<V>> putAndGet(K key, V value, Duration ttl) {
        return delegate().putAndGet(key, value, ttl);
    }

    @Override
    public CompletableFuture<Versioned<V>> remove(K key) {
        return delegate().remove(key);
    }

    @Override
    public CompletableFuture<Void> clear() {
        return delegate().clear();
    }

    @Override
    public AsyncDistributedSet<K> keySet() {
        return delegate().keySet();
    }

    @Override
    public AsyncDistributedCollection<Versioned<V>> values() {
        return delegate().values();
    }

    @Override
    public AsyncDistributedSet<Map.Entry<K, Versioned<V>>> entrySet() {
        return delegate().entrySet();
    }

    @Override
    public CompletableFuture<Versioned<V>> putIfAbsent(K key, V value) {
        return delegate().putIfAbsent(key, value);
    }

    @Override
    public CompletableFuture<Versioned<V>> putIfAbsent(K key, V value, Duration ttl) {
        return delegate().putIfAbsent(key, value, ttl);
    }

    @Override
    public CompletableFuture<Boolean> remove(K key, V value) {
        return delegate().remove(key, value);
    }

    @Override
    public CompletableFuture<Boolean> remove(K key, long version) {
        return delegate().remove(key, version);
    }

    @Override
    public CompletableFuture<Versioned<V>> replace(K key, V value) {
        return delegate().replace(key, value);
    }

    @Override
    public CompletableFuture<Boolean> replace(K key, V oldValue, V newValue) {
        return delegate().replace(key, oldValue, newValue);
    }

    @Override
    public CompletableFuture<Boolean> replace(K key, long oldVersion, V newValue) {
        return delegate().replace(key, oldVersion, newValue);
    }

    @Override
    public CompletableFuture<Void> lock(K key) {
        return delegate().lock(key);
    }

    @Override
    public CompletableFuture<Boolean> tryLock(K key) {
        return delegate().tryLock(key);
    }

    @Override
    public CompletableFuture<Boolean> tryLock(K key, Duration timeout) {
        return delegate().tryLock(key, timeout);
    }

    @Override
    public CompletableFuture<Boolean> isLocked(K key) {
        return delegate().isLocked(key);
    }

    @Override
    public CompletableFuture<Void> unlock(K key) {
        return delegate().unlock(key);
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicMapEventListener<K, V> listener, Executor executor) {
        return delegate().listen(listener, executor);
    }

    @Override
    public AtomicMap<K, V> sync(Duration operationTimeout) {
        return new BlockingAtomicMap<>(this, operationTimeout);
    }
}
