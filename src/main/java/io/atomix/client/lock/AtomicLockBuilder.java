package io.atomix.client.lock;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;

/**
 * Builder for AtomicLock.
 */
public abstract class AtomicLockBuilder extends PrimitiveBuilder<AtomicLockBuilder, AtomicLock> {
    protected AtomicLockBuilder(AtomixChannel channel) {
        super(channel);
    }
}
