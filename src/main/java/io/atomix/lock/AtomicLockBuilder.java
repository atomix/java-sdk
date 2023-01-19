package io.atomix.lock;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;
import io.atomix.api.lock.v1.LockGrpc;

/**
 * Builder for AtomicLock.
 */
public abstract class AtomicLockBuilder extends PrimitiveBuilder<AtomicLockBuilder, AtomicLock, LockGrpc.LockStub> {

    protected AtomicLockBuilder(AtomixChannel channel) {
        super(channel, LockGrpc.newStub(channel), channel.executor());
    }
}
