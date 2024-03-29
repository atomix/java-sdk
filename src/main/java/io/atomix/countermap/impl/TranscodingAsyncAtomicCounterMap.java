package io.atomix.countermap.impl;

import io.atomix.DelegatingAsyncPrimitive;
import io.atomix.countermap.AsyncAtomicCounterMap;
import io.atomix.countermap.AtomicCounterMap;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * An {@code AsyncAtomicCounterMap} that transcodes keys.
 */
public class TranscodingAsyncAtomicCounterMap<K1, K2>
    extends DelegatingAsyncPrimitive<AsyncAtomicCounterMap<K1>, AtomicCounterMap<K1>, AsyncAtomicCounterMap<K2>>
    implements AsyncAtomicCounterMap<K1> {
    private final AsyncAtomicCounterMap<K2> backingMap;
    private final Function<K1, K2> keyEncoder;

    public TranscodingAsyncAtomicCounterMap(AsyncAtomicCounterMap<K2> backingMap, Function<K1, K2> keyEncoder) {
        super(backingMap);
        this.backingMap = backingMap;
        this.keyEncoder = k -> k == null ? null : keyEncoder.apply(k);
    }

    @Override
    public CompletableFuture<Long> incrementAndGet(K1 key) {
        try {
            return backingMap.incrementAndGet(keyEncoder.apply(key));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> decrementAndGet(K1 key) {
        try {
            return backingMap.decrementAndGet(keyEncoder.apply(key));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> getAndIncrement(K1 key) {
        try {
            return backingMap.getAndIncrement(keyEncoder.apply(key));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> getAndDecrement(K1 key) {
        try {
            return backingMap.getAndDecrement(keyEncoder.apply(key));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> addAndGet(K1 key, long delta) {
        try {
            return backingMap.addAndGet(keyEncoder.apply(key), delta);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> getAndAdd(K1 key, long delta) {
        try {
            return backingMap.getAndAdd(keyEncoder.apply(key), delta);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> get(K1 key) {
        try {
            return backingMap.get(keyEncoder.apply(key));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> put(K1 key, long newValue) {
        try {
            return backingMap.put(keyEncoder.apply(key), newValue);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> putIfAbsent(K1 key, long newValue) {
        try {
            return backingMap.putIfAbsent(keyEncoder.apply(key), newValue);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> replace(K1 key, long expectedOldValue, long newValue) {
        try {
            return backingMap.replace(keyEncoder.apply(key), expectedOldValue, newValue);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Long> remove(K1 key) {
        try {
            return backingMap.remove(keyEncoder.apply(key));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Boolean> remove(K1 key, long value) {
        try {
            return backingMap.remove(keyEncoder.apply(key), value);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Integer> size() {
        try {
            return backingMap.size();
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
    public CompletableFuture<Void> clear() {
        try {
            return backingMap.clear();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
