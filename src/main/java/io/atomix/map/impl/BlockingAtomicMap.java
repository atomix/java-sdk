// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map.impl;

import io.atomix.Cancellable;
import io.atomix.Synchronous;
import io.atomix.collection.DistributedCollection;
import io.atomix.collection.impl.BlockingDistributedCollection;
import io.atomix.map.AsyncAtomicMap;
import io.atomix.map.AtomicMap;
import io.atomix.map.AtomicMapEventListener;
import io.atomix.set.DistributedSet;
import io.atomix.set.impl.BlockingDistributedSet;
import io.atomix.time.Versioned;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Default implementation of {@code ConsistentMap}.
 *
 * @param <K> type of key.
 * @param <V> type of value.
 */
public class BlockingAtomicMap<K, V> extends Synchronous<AtomicMap<K, V>, AsyncAtomicMap<K, V>> implements AtomicMap<K, V> {

    private static final int MAX_DELAY_BETWEEN_RETRY_MILLS = 50;
    private final AsyncAtomicMap<K, V> asyncMap;

    public BlockingAtomicMap(AsyncAtomicMap<K, V> asyncMap, Duration operationTimeout) {
        super(asyncMap, operationTimeout);
        this.asyncMap = asyncMap;
    }

    @Override
    public int size() {
        return complete(asyncMap.size());
    }

    @Override
    public boolean isEmpty() {
        return complete(asyncMap.isEmpty());
    }

    @Override
    public boolean containsKey(K key) {
        return complete(asyncMap.containsKey(key));
    }

    @Override
    public boolean containsValue(V value) {
        return complete(asyncMap.containsValue(value));
    }

    @Override
    public Versioned<V> get(K key) {
        return complete(asyncMap.get(key));
    }

    @Override
    public Versioned<V> getOrDefault(K key, V defaultValue) {
        return complete(asyncMap.getOrDefault(key, defaultValue));
    }

    @Override
    public Versioned<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return complete(asyncMap.computeIfAbsent(key, mappingFunction));
    }

    @Override
    public Versioned<V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return complete(asyncMap.compute(key, remappingFunction));
    }

    @Override
    public Versioned<V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return complete(asyncMap.computeIfPresent(key, remappingFunction));
    }

    @Override
    public Versioned<V> computeIf(K key, Predicate<? super V> condition, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return complete(asyncMap.computeIf(key, condition, remappingFunction));
    }

    @Override
    public Versioned<V> put(K key, V value, Duration ttl) {
        return complete(asyncMap.put(key, value, ttl));
    }

    @Override
    public Versioned<V> putAndGet(K key, V value, Duration ttl) {
        return complete(asyncMap.putAndGet(key, value, ttl));
    }

    @Override
    public Versioned<V> remove(K key) {
        return complete(asyncMap.remove(key));
    }

    @Override
    public void clear() {
        complete(asyncMap.clear());
    }

    @Override
    public DistributedSet<K> keySet() {
        return new BlockingDistributedSet<K>(asyncMap.keySet(), operationTimeout);
    }

    @Override
    public DistributedCollection<Versioned<V>> values() {
        return new BlockingDistributedCollection<>(asyncMap.values(), operationTimeout);
    }

    @Override
    public DistributedSet<Map.Entry<K, Versioned<V>>> entrySet() {
        return new BlockingDistributedSet<>(asyncMap.entrySet(), operationTimeout);
    }

    @Override
    public Versioned<V> putIfAbsent(K key, V value, Duration ttl) {
        return complete(asyncMap.putIfAbsent(key, value, ttl));
    }

    @Override
    public boolean remove(K key, V value) {
        return complete(asyncMap.remove(key, value));
    }

    @Override
    public boolean remove(K key, long version) {
        return complete(asyncMap.remove(key, version));
    }

    @Override
    public Versioned<V> replace(K key, V value) {
        return complete(asyncMap.replace(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return complete(asyncMap.replace(key, oldValue, newValue));
    }

    @Override
    public boolean replace(K key, long oldVersion, V newValue) {
        return complete(asyncMap.replace(key, oldVersion, newValue));
    }

    @Override
    public void lock(K key) {
        complete(asyncMap.lock(key));
    }

    @Override
    public boolean tryLock(K key) {
        return complete(asyncMap.tryLock(key));
    }

    @Override
    public boolean tryLock(K key, Duration timeout) {
        return complete(asyncMap.tryLock(key, timeout));
    }

    @Override
    public boolean isLocked(K key) {
        return complete(asyncMap.isLocked(key));
    }

    @Override
    public void unlock(K key) {
        complete(asyncMap.unlock(key));
    }

    @Override
    public Cancellable listen(AtomicMapEventListener<K, V> listener, Executor executor) {
        return complete(asyncMap.listen(listener, executor));
    }

    @Override
    public AsyncAtomicMap<K, V> async() {
        return asyncMap;
    }
}