package io.atomix.client.lock.impl;

import io.atomix.api.runtime.lock.v1.LockGrpc;
import io.atomix.client.AtomixChannel;
import io.atomix.client.lock.AsyncDistributedLock;
import io.atomix.client.lock.DistributedLock;
import io.atomix.client.lock.DistributedLockBuilder;

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
