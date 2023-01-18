package io.atomix.lock.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.lock.v1.LockGrpc;
import io.atomix.lock.AsyncAtomicLock;
import io.atomix.lock.AtomicLock;
import io.atomix.lock.AtomicLockBuilder;

import java.util.concurrent.CompletableFuture;

public class DefaultAtomicLockBuilder extends AtomicLockBuilder {

    public DefaultAtomicLockBuilder(AtomixChannel channel) {
        super(channel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicLock> buildAsync() {
        return new DefaultAsyncAtomicLock(name(), LockGrpc.newStub(channel()), channel().executor())
            .create(tags())
            .thenApply(AsyncAtomicLock::sync);
    }
}
