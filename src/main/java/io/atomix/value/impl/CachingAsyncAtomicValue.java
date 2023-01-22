// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.value.impl;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.Cancellable;
import io.atomix.time.Versioned;
import io.atomix.value.AsyncAtomicValue;
import io.atomix.value.AtomicValueEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * {@code AsyncAtomicValue} that caches value on read.
 * <p>
 * The cache is automatically invalidated when updates are detected either locally or
 * remotely.
 * <p> This implementation only attempts to serve cached entries for
 * {@link AsyncAtomicValue#get()} get}.
 *
 * @param <V> identifier type
 */
public class CachingAsyncAtomicValue<V> extends DelegatingAsyncAtomicValue<V> {
    private CompletableFuture<Versioned<V>> cache;
    private AtomicValueEventListener<V> valueListener;
    private final AtomicValueEventListener<V> cacheUpdater;

    public CachingAsyncAtomicValue(AsyncAtomicValue<V> delegateValue) {
        super(delegateValue);
        cacheUpdater = event -> {
            cache = CompletableFuture.completedFuture(event.newValue());
            if (valueListener != null) {
                valueListener.event(event);
            }
        };
        super.listen(cacheUpdater, MoreExecutors.directExecutor());
    }

    @Override
    public CompletableFuture<Versioned<V>> get() {
        // Technically speaking, it substitutes the cache loader
        if (cache == null) {
            cache = super.get();
        }
        return cache.whenComplete((r, e) -> {
            if (e != null) {
                cache = null;
            }
        });
    }

    @Override
    public CompletableFuture<Versioned<V>> set(V value) {
        return super.set(value).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Versioned<V>> set(V value, long version) {
        return super.set(value, version).whenComplete((r, e) -> cache = null);
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicValueEventListener<V> listener, Executor executor) {
        valueListener = listener;
        // On cancel removes itself
        return CompletableFuture.completedFuture(() -> valueListener = null);
    }
}
