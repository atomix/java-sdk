package io.atomix.lock;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;
import io.atomix.api.lock.v1.LockGrpc;

/**
 * Builder for DistributedLock.
 */
public abstract class DistributedLockBuilder extends PrimitiveBuilder<DistributedLockBuilder, DistributedLock, LockGrpc.LockStub> {

    protected DistributedLockBuilder(AtomixChannel channel) {
        super(channel, LockGrpc.newStub(channel), channel.executor());
    }
}
