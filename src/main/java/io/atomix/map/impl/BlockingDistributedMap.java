package io.atomix.map.impl;

import io.atomix.Cancellable;
import io.atomix.Synchronous;
import io.atomix.collection.DistributedCollection;
import io.atomix.collection.impl.BlockingDistributedCollection;
import io.atomix.map.AsyncDistributedMap;
import io.atomix.map.DistributedMap;
import io.atomix.map.MapEventListener;
import io.atomix.set.DistributedSet;
import io.atomix.set.impl.BlockingDistributedSet;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/**
 * Default implementation of {@code ConsistentMap}.
 *
 * @param <K> type of key.
 * @param <V> type of value.
 */
public class BlockingDistributedMap<K, V> extends Synchronous<DistributedMap<K, V>, AsyncDistributedMap<K, V>> implements DistributedMap<K, V> {
    private final AsyncDistributedMap<K, V> asyncMap;

    public BlockingDistributedMap(AsyncDistributedMap<K, V> asyncMap, Duration operationTimeout) {
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
    public boolean containsKey(Object key) {
        return complete(asyncMap.containsKey((K) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return complete(asyncMap.containsValue((V) value));
    }

    @Override
    public V put(K key, V value) {
        return complete(asyncMap.put(key, value));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        complete(asyncMap.putAll(m));
    }

    @Override
    public V get(Object key) {
        return complete(asyncMap.get((K) key));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return complete(asyncMap.getOrDefault((K) key, defaultValue));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return complete(asyncMap.compute((K) key, remappingFunction));
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        return complete(asyncMap.remove((K) key));
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
    public DistributedCollection<V> values() {
        return new BlockingDistributedCollection<>(asyncMap.values(), operationTimeout);
    }

    @Override
    public DistributedSet<Map.Entry<K, V>> entrySet() {
        return new BlockingDistributedSet<>(asyncMap.entrySet(), operationTimeout);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return complete(asyncMap.remove((K) key, (V) value));
    }

    @Override
    public V replace(K key, V value) {
        return complete(asyncMap.replace(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return complete(asyncMap.replace(key, oldValue, newValue));
    }

    @Override
    public Cancellable listen(MapEventListener<K, V> listener, Executor executor) {
        return complete(asyncMap.listen(listener, executor));
    }

    @Override
    public AsyncDistributedMap<K, V> async() {
        return asyncMap;
    }
}
