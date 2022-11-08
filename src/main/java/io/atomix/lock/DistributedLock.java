package io.atomix.lock;

import io.atomix.AtomixChannel;
import io.atomix.SyncPrimitive;
import io.atomix.lock.impl.DefaultDistributedLockBuilder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public interface DistributedLock extends SyncPrimitive<DistributedLock, AsyncDistributedLock>, Lock {

    /**
     * Returns a new DistributedLock builder.
     *
     * @return the DistributedLock builder
     */
    static DistributedLockBuilder builder() {
        return builder(AtomixChannel.instance());
    }

    /**
     * Returns a new DistributedLock builder.
     *
     * @param channel the AtomixChannel
     * @return the DistributedLock builder
     */
    static DistributedLockBuilder builder(AtomixChannel channel) {
        return new DefaultDistributedLockBuilder(channel);
    }

    @Override
    default boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryLock(Duration.ofMillis(unit.toMillis(time)));
    }

    /**
     * Acquires the lock if it is free within the given waiting time and the
     * current thread has not been {@linkplain Thread#interrupt interrupted}.
     *
     * @param timeout the timeout to wait to acquire the lock
     * @throws InterruptedException if the current thread is interrupted
     *                              while acquiring the lock (and interruption of lock
     *                              acquisition is supported)
     */
    boolean tryLock(Duration timeout) throws InterruptedException;

    /**
     * Query whether this lock is locked or not.
     *
     * @return {@code true} if this lock is locked, {@code false} otherwise
     */
    boolean isLocked();

    @Override
    AsyncDistributedLock async();
}
