package io.atomix.lock;

import io.atomix.AsyncPrimitive;
import io.atomix.lock.impl.BlockingDistributedLock;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface AsyncDistributedLock extends AsyncPrimitive<AsyncDistributedLock, DistributedLock> {

    /**
     * Acquires the lock, blocking until it's available.
     *
     * @return future to be completed once the lock has been acquired
     */
    CompletableFuture<Void> lock();

    /**
     * Attempts to acquire the lock.
     *
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    CompletableFuture<Boolean> tryLock();

    /**
     * Attempts to acquire the lock.
     *
     * @param timeout the timeout after which to give up attempting to acquire the lock
     * @param unit    the timeout time unit
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    default CompletableFuture<Boolean> tryLock(long timeout, TimeUnit unit) {
        return tryLock(Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * Attempts to acquire the lock for a specified amount of time.
     *
     * @param timeout the timeout after which to give up attempting to acquire the lock
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    CompletableFuture<Boolean> tryLock(Duration timeout);

    /**
     * Unlocks the lock.
     *
     * @return future to be completed once the lock has been released
     */
    CompletableFuture<Void> unlock();

    /**
     * Query whether this lock is locked or not.
     *
     * @return future to be completed with a boolean indicating whether the lock was locked or not
     */
    CompletableFuture<Boolean> isLocked();

    @Override
    default DistributedLock sync(Duration operationTimeout) {
        return new BlockingDistributedLock(this, operationTimeout);
    }
}
