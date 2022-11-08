package io.atomix.lock;

import io.atomix.AsyncPrimitive;
import io.atomix.lock.impl.BlockingAtomicLock;

import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface AsyncAtomicLock extends AsyncPrimitive<AsyncAtomicLock, AtomicLock> {

    /**
     * Acquires the lock, blocking until it's available.
     *
     * @return future to be completed once the lock has been acquired
     */
    CompletableFuture<Long> lock();

    /**
     * Attempts to acquire the lock.
     *
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    CompletableFuture<OptionalLong> tryLock();

    /**
     * Attempts to acquire the lock.
     *
     * @param timeout the timeout after which to give up attempting to acquire the lock
     * @param unit    the timeout time unit
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    default CompletableFuture<OptionalLong> tryLock(long timeout, TimeUnit unit) {
        return tryLock(Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * Attempts to acquire the lock for a specified amount of time.
     *
     * @param timeout the timeout after which to give up attempting to acquire the lock
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    CompletableFuture<OptionalLong> tryLock(Duration timeout);

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
    default AtomicLock sync(Duration operationTimeout) {
        return new BlockingAtomicLock(this, operationTimeout);
    }
}
