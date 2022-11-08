package io.atomix.lock;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;

/**
 * Builder for AtomicLock.
 */
public abstract class AtomicLockBuilder extends PrimitiveBuilder<AtomicLockBuilder, AtomicLock> {
    protected AtomicLockBuilder(AtomixChannel channel) {
        super(channel);
    }
}
