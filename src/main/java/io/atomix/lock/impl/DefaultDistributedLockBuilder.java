package io.atomix.lock.impl;

import io.atomix.AtomixChannel;
import io.atomix.api.lock.v1.CreateRequest;
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
        // Creates the primitive and connect. Eventually, returns a future
        // to be completed once the primitive is created and connected
        DefaultAsyncAtomicLock rawLock = new DefaultAsyncAtomicLock(name(), this.stub, this.executorService);
        return retry(LockGrpc.LockStub::create, CreateRequest.newBuilder()
                .setId(id())
                .addAllTags(tags())
                .build())
                .thenApply(response -> new DelegatingAsyncDistributedLock(rawLock))
                .thenApply(AsyncDistributedLock::sync);
    }
}
