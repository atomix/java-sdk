package io.atomix.client.lock;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;

/**
 * Builder for DistributedLock.
 */
public abstract class DistributedLockBuilder extends PrimitiveBuilder<DistributedLockBuilder, DistributedLock> {
    protected DistributedLockBuilder(AtomixChannel channel) {
        super(channel);
    }
}
