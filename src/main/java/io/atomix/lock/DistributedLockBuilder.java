package io.atomix.lock;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;

/**
 * Builder for DistributedLock.
 */
public abstract class DistributedLockBuilder extends PrimitiveBuilder<DistributedLockBuilder, DistributedLock> {
    protected DistributedLockBuilder(AtomixChannel channel) {
        super(channel);
    }
}
