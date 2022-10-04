package io.atomix.client.lock.impl;

import io.atomix.client.Synchronous;
import io.atomix.client.lock.AsyncDistributedLock;
import io.atomix.client.lock.DistributedLock;

import java.time.Duration;
import java.util.concurrent.locks.Condition;

public class BlockingDistributedLock extends Synchronous<DistributedLock, AsyncDistributedLock> implements DistributedLock {

    private final AsyncDistributedLock asyncLock;

    public BlockingDistributedLock(AsyncDistributedLock asyncLock, Duration operationTimeout) {
        super(asyncLock, operationTimeout);
        this.asyncLock = asyncLock;
    }

    @Override
    public boolean tryLock(Duration timeout) throws InterruptedException {
        return complete(asyncLock.tryLock(timeout));
    }

    @Override
    public boolean isLocked() {
        return complete(asyncLock.isLocked());
    }

    @Override
    public void lock() {
        complete(asyncLock.lock());
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // TODO: Implement lock interruptions via Context cancellation
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        return complete(asyncLock.tryLock());
    }

    @Override
    public void unlock() {
        complete(asyncLock.unlock());
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncDistributedLock async() {
        return asyncLock;
    }
}
