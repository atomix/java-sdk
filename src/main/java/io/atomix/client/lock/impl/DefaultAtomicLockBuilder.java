package io.atomix.client.lock.impl;

import io.atomix.api.runtime.lock.v1.LockGrpc;
import io.atomix.client.AtomixChannel;
import io.atomix.client.lock.AsyncAtomicLock;
import io.atomix.client.lock.AtomicLock;
import io.atomix.client.lock.AtomicLockBuilder;

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
