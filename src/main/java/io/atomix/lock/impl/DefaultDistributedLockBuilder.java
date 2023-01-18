package io.atomix.lock.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.lock.v1.LockGrpc;
import io.atomix.lock.AsyncDistributedLock;
import io.atomix.lock.DistributedLock;
import io.atomix.lock.DistributedLockBuilder;

import java.util.concurrent.CompletableFuture;

public class DefaultDistributedLockBuilder extends DistributedLockBuilder {

    public DefaultDistributedLockBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    public CompletableFuture<DistributedLock> buildAsync() {
        return new DefaultAsyncAtomicLock(name(), LockGrpc.newStub(channel()), channel().executor()).create(tags())
            .thenApply(DelegatingAsyncDistributedLock::new)
            .thenApply(AsyncDistributedLock::sync);
    }
}
