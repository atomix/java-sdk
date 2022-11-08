package io.atomix.client.lock;

import io.atomix.client.AtomixChannel;
import io.atomix.client.SyncPrimitive;
import io.atomix.client.lock.impl.DefaultAtomicLockBuilder;
import io.atomix.client.lock.impl.DefaultDistributedLockBuilder;

import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

public interface AtomicLock extends SyncPrimitive<AtomicLock, AsyncAtomicLock> {

    /**
     * Returns a new AtomicLock builder.
     *
     * @return the AtomicLock builder
     */
    static AtomicLockBuilder builder() {
        return builder(AtomixChannel.instance());
    }

    /**
     * Returns a new AtomicLock builder.
     *
     * @param channel the AtomixChannel
     * @return the AtomicLock builder
     */
    static AtomicLockBuilder builder(AtomixChannel channel) {
        return new DefaultAtomicLockBuilder(channel);
    }

    /**
     * Acquires the lock, blocking until it's available.
     *
     * @return future to be completed once the lock has been acquired
     */
    long lock();

    /**
     * Attempts to acquire the lock.
     *
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    OptionalLong tryLock();

    /**
     * Attempts to acquire the lock.
     *
     * @param timeout the timeout after which to give up attempting to acquire the lock
     * @param unit    the timeout time unit
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    default OptionalLong tryLock(long timeout, TimeUnit unit) {
        return tryLock(Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * Attempts to acquire the lock for a specified amount of time.
     *
     * @param timeout the timeout after which to give up attempting to acquire the lock
     * @return future to be completed with a boolean indicating whether the lock was acquired
     */
    OptionalLong tryLock(Duration timeout);

    /**
     * Unlocks the lock.
     */
    void unlock();

    /**
     * Query whether this lock is locked or not.
     *
     * @return future to be completed with a boolean indicating whether the lock was locked or not
     */
    boolean isLocked();
}
