package io.atomix.client.lock.impl;

import io.atomix.client.DelegatingAsyncPrimitive;
import io.atomix.client.lock.AsyncAtomicLock;
import io.atomix.client.lock.AsyncDistributedLock;
import io.atomix.client.lock.DistributedLock;

import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

public class DelegatingAsyncDistributedLock
    extends DelegatingAsyncPrimitive<AsyncDistributedLock, DistributedLock, AsyncAtomicLock>
    implements AsyncDistributedLock {

    public DelegatingAsyncDistributedLock(AsyncAtomicLock atomicLock) {
        super(atomicLock);
    }

    @Override
    public CompletableFuture<Void> lock() {
        return delegate().lock().thenApply(v -> null);
    }

    @Override
    public CompletableFuture<Boolean> tryLock() {
        return delegate().tryLock().thenApply(OptionalLong::isPresent);
    }

    @Override
    public CompletableFuture<Boolean> tryLock(Duration timeout) {
        return delegate().tryLock(timeout).thenApply(OptionalLong::isPresent);
    }

    @Override
    public CompletableFuture<Void> unlock() {
        return delegate().unlock();
    }

    @Override
    public CompletableFuture<Boolean> isLocked() {
        return delegate().isLocked();
    }
}
