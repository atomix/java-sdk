package io.atomix.lock.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.lock.v1.CreateRequest;
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
        return retry(LockGrpc.LockStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> new DefaultAsyncAtomicLock(name(), this.stub, this.executorService))
                .thenApply(AsyncAtomicLock::sync);
    }
}
