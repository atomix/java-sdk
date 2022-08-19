package io.atomix.client.map.impl;

import com.google.common.base.Throwables;
import io.atomix.client.Cancellable;
import io.atomix.client.PrimitiveException;
import io.atomix.client.Synchronous;
import io.atomix.client.collection.DistributedCollection;
import io.atomix.client.collection.impl.BlockingDistributedCollection;
import io.atomix.client.map.AsyncDistributedMap;
import io.atomix.client.map.DistributedMap;
import io.atomix.client.map.MapEventListener;
import io.atomix.client.set.DistributedSet;
import io.atomix.client.set.impl.BlockingDistributedSet;
import io.atomix.client.utils.concurrent.Retries;

import java.time.Duration;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation of {@code ConsistentMap}.
 *
 * @param <K> type of key.
 * @param <V> type of value.
 */
public class BlockingDistributedMap<K, V> extends Synchronous<AsyncDistributedMap<K, V>> implements DistributedMap<K, V> {

    private static final int MAX_DELAY_BETWEEN_RETRY_MILLS = 50;
    private final AsyncDistributedMap<K, V> asyncMap;
    private final long operationTimeoutMillis;

    public BlockingDistributedMap(AsyncDistributedMap<K, V> asyncMap, long operationTimeoutMillis) {
        super(asyncMap);
        this.asyncMap = asyncMap;
        this.operationTimeoutMillis = operationTimeoutMillis;
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
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Retries.retryable(() -> complete(asyncMap.computeIfAbsent(key, mappingFunction)),
            PrimitiveException.ConcurrentModification.class,
            Integer.MAX_VALUE,
            MAX_DELAY_BETWEEN_RETRY_MILLS).get();
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Retries.retryable(() -> complete(asyncMap.computeIfPresent(key, remappingFunction)),
            PrimitiveException.ConcurrentModification.class,
            Integer.MAX_VALUE,
            MAX_DELAY_BETWEEN_RETRY_MILLS).get();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Retries.retryable(() -> complete(asyncMap.compute(key, remappingFunction)),
            PrimitiveException.ConcurrentModification.class,
            Integer.MAX_VALUE,
            MAX_DELAY_BETWEEN_RETRY_MILLS).get();
    }

    @Override
    public V remove(Object key) {
        return complete(asyncMap.remove((K) key));
    }

    @Override
    public void clear() {
        complete(asyncMap.clear());
    }

    @Override
    public DistributedSet<K> keySet() {
        return new BlockingDistributedSet<K>(asyncMap.keySet(), operationTimeoutMillis);
    }

    @Override
    public DistributedCollection<V> values() {
        return new BlockingDistributedCollection<>(asyncMap.values(), operationTimeoutMillis);
    }

    @Override
    public DistributedSet<Map.Entry<K, V>> entrySet() {
        return new BlockingDistributedSet<>(asyncMap.entrySet(), operationTimeoutMillis);
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
    public Cancellable listen(MapEventListener<K, V> listener, Executor executor) {
        return complete(asyncMap.listen(listener, executor));
    }

    @Override
    public AsyncDistributedMap<K, V> async() {
        return asyncMap;
    }

    protected <T> T complete(CompletableFuture<T> future) {
        try {
            return future.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrimitiveException.Interrupted();
        } catch (TimeoutException e) {
            throw new PrimitiveException.Timeout();
        } catch (ExecutionException e) {
            Throwable cause = Throwables.getRootCause(e);
            if (cause instanceof PrimitiveException) {
                throw (PrimitiveException) cause;
            } else if (cause instanceof ConcurrentModificationException) {
                throw (ConcurrentModificationException) cause;
            } else {
                throw new PrimitiveException(cause);
            }
        }
    }
}
