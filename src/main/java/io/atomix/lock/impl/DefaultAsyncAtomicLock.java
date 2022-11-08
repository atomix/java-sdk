package io.atomix.lock.impl;

import io.atomix.api.runtime.lock.v1.CloseRequest;
import io.atomix.api.runtime.lock.v1.CreateRequest;
import io.atomix.api.runtime.lock.v1.GetLockRequest;
import io.atomix.api.runtime.lock.v1.LockGrpc;
import io.atomix.api.runtime.lock.v1.LockRequest;
import io.atomix.api.runtime.lock.v1.LockResponse;
import io.atomix.api.runtime.lock.v1.UnlockRequest;
import io.atomix.impl.AbstractAsyncPrimitive;
import io.atomix.lock.AsyncAtomicLock;
import io.atomix.lock.AtomicLock;
import io.grpc.Status;

import java.time.Duration;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

public class DefaultAsyncAtomicLock
    extends AbstractAsyncPrimitive<AsyncAtomicLock, AtomicLock, LockGrpc.LockStub>
    implements AsyncAtomicLock {

    public DefaultAsyncAtomicLock(String name, LockGrpc.LockStub stub, ScheduledExecutorService executorService) {
        super(name, stub, executorService);
    }

    @Override
    protected CompletableFuture<AsyncAtomicLock> create(Set<String> tags) {
        return retry(LockGrpc.LockStub::create, CreateRequest.newBuilder()
            .setId(id())
            .addAllTags(tags)
            .build())
            .thenApply(response -> this);
    }

    @Override
    public CompletableFuture<Void> close() {
        return retry(LockGrpc.LockStub::close, CloseRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Long> lock() {
        return retry(LockGrpc.LockStub::lock, LockRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(LockResponse::getVersion);
    }

    @Override
    public CompletableFuture<OptionalLong> tryLock() {
        return retry(LockGrpc.LockStub::lock, LockRequest.newBuilder()
            .setId(id())
            .setTimeout(com.google.protobuf.Duration.newBuilder()
                .setSeconds(0)
                .setNanos(0)
                .build())
            .build())
            .thenApply(response -> OptionalLong.of(response.getVersion()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return OptionalLong.empty();
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<OptionalLong> tryLock(Duration timeout) {
        return retry(LockGrpc.LockStub::lock, LockRequest.newBuilder()
            .setId(id())
            .setTimeout(com.google.protobuf.Duration.newBuilder()
                .setSeconds(timeout.getSeconds())
                .setNanos(timeout.getNano())
                .build())
            .build())
            .thenApply(response -> OptionalLong.of(response.getVersion()))
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.ABORTED) {
                    return OptionalLong.empty();
                } else {
                    throw (RuntimeException) t;
                }
            });
    }

    @Override
    public CompletableFuture<Void> unlock() {
        return retry(LockGrpc.LockStub::unlock, UnlockRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<Boolean> isLocked() {
        return retry(LockGrpc.LockStub::getLock, GetLockRequest.newBuilder()
            .setId(id())
            .build())
            .thenApply(response -> true)
            .exceptionally(t -> {
                if (Status.fromThrowable(t).getCode() == Status.Code.NOT_FOUND) {
                    return false;
                } else {
                    throw (RuntimeException) t;
                }
            });
    }
}
