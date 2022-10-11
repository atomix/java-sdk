package io.atomix.client.countermap.impl;

import io.atomix.client.Synchronous;
import io.atomix.client.countermap.AsyncAtomicCounterMap;
import io.atomix.client.countermap.AtomicCounterMap;

import java.time.Duration;

/**
 * Default implementation of {@code AtomicCounterMap}.
 *
 * @param <K> map key type
 */
public class BlockingAtomicCounterMap<K> extends Synchronous<AtomicCounterMap<K>, AsyncAtomicCounterMap<K>> implements AtomicCounterMap<K> {

    private final AsyncAtomicCounterMap<K> asyncCounterMap;

    public BlockingAtomicCounterMap(AsyncAtomicCounterMap<K> asyncCounterMap, Duration operationTimeout) {
        super(asyncCounterMap, operationTimeout);
        this.asyncCounterMap = asyncCounterMap;
    }

    @Override
    public long incrementAndGet(K key) {
        return complete(asyncCounterMap.incrementAndGet(key));
    }

    @Override
    public long decrementAndGet(K key) {
        return complete(asyncCounterMap.decrementAndGet(key));
    }

    @Override
    public long getAndIncrement(K key) {
        return complete(asyncCounterMap.getAndIncrement(key));
    }

    @Override
    public long getAndDecrement(K key) {
        return complete(asyncCounterMap.getAndDecrement(key));
    }

    @Override
    public long addAndGet(K key, long delta) {
        return complete(asyncCounterMap.addAndGet(key, delta));
    }

    @Override
    public long getAndAdd(K key, long delta) {
        return complete(asyncCounterMap.getAndAdd(key, delta));
    }

    @Override
    public long get(K key) {
        return complete(asyncCounterMap.get(key));
    }

    @Override
    public long put(K key, long newValue) {
        return complete(asyncCounterMap.put(key, newValue));
    }

    @Override
    public long putIfAbsent(K key, long newValue) {
        return complete(asyncCounterMap.putIfAbsent(key, newValue));
    }

    @Override
    public boolean replace(K key, long expectedOldValue, long newValue) {
        return complete(asyncCounterMap.replace(key, expectedOldValue, newValue));
    }

    @Override
    public long remove(K key) {
        return complete(asyncCounterMap.remove(key));
    }

    @Override
    public boolean remove(K key, long value) {
        return complete(asyncCounterMap.remove(key, value));
    }

    @Override
    public int size() {
        return complete(asyncCounterMap.size());
    }

    @Override
    public boolean isEmpty() {
        return complete(asyncCounterMap.isEmpty());
    }

    @Override
    public void clear() {
        complete(asyncCounterMap.clear());
    }

    @Override
    public AsyncAtomicCounterMap<K> async() {
        return asyncCounterMap;
    }
}
