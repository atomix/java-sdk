package io.atomix.lock.impl;

import io.atomix.Synchronous;
import io.atomix.lock.AsyncAtomicLock;
import io.atomix.lock.AtomicLock;

import java.time.Duration;
import java.util.OptionalLong;

public class BlockingAtomicLock extends Synchronous<AtomicLock, AsyncAtomicLock> implements AtomicLock {

    private final AsyncAtomicLock asyncLock;

    public BlockingAtomicLock(AsyncAtomicLock asyncLock, Duration operationTimeout) {
        super(asyncLock, operationTimeout);
        this.asyncLock = asyncLock;
    }

    @Override
    public boolean isLocked() {
        return complete(asyncLock.isLocked());
    }

    @Override
    public long lock() {
        return complete(asyncLock.lock());
    }

    @Override
    public OptionalLong tryLock() {
        return complete(asyncLock.tryLock());
    }

    @Override
    public OptionalLong tryLock(Duration timeout) {
        return complete(asyncLock.tryLock(timeout));
    }

    @Override
    public void unlock() {
        complete(asyncLock.unlock());
    }

    @Override
    public AsyncAtomicLock async() {
        return asyncLock;
    }
}
