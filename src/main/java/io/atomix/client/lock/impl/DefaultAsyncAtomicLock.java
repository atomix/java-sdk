/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client.lock.impl;

import io.atomix.api.lock.*;
import io.atomix.api.primitive.Name;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.lock.AsyncAtomicLock;
import io.atomix.client.lock.AtomicLock;
import io.atomix.client.session.Session;
import io.atomix.client.utils.concurrent.Scheduled;
import io.atomix.client.utils.concurrent.ThreadContext;

import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Raft lock.
 */
public class DefaultAsyncAtomicLock extends AbstractAsyncPrimitive<LockServiceGrpc.LockServiceStub, AsyncAtomicLock> implements AsyncAtomicLock {
    private final AtomicLong lockId = new AtomicLong();

    public DefaultAsyncAtomicLock(Name name, Session session, ThreadContext context) {
        super(name, LockServiceGrpc.newStub(session.getPartition().getChannelFactory().getChannel()), session, context);
    }

    @Override
    public CompletableFuture<Long> lock() {
        return command(
            (header, observer) -> getService().lock(LockRequest.newBuilder()
                .setHeader(header)
                .setTimeout(com.google.protobuf.Duration.newBuilder()
                    .setSeconds(-1)
                    .build())
                .build(), observer), LockResponse::getHeader)
            .thenApply(response -> {
                lockId.set(response.getVersion());
                return response.getVersion();
            });
    }

    @Override
    public CompletableFuture<OptionalLong> tryLock() {
        return command(
            (header, observer) -> getService().lock(LockRequest.newBuilder()
                .setHeader(header)
                .setTimeout(com.google.protobuf.Duration.newBuilder()
                    .setSeconds(0)
                    .setNanos(0)
                    .build())
                .build(), observer), LockResponse::getHeader)
            .thenApply(response -> response.getVersion() > 0 ? OptionalLong.of(response.getVersion()) : OptionalLong.empty())
            .thenApply(version -> {
                if (version.isPresent()) {
                    lockId.set(version.getAsLong());
                }
                return version;
            });
    }

    @Override
    public CompletableFuture<OptionalLong> tryLock(Duration timeout) {
        CompletableFuture<OptionalLong> future = new CompletableFuture<>();
        Scheduled timer = context().schedule(timeout, () -> future.complete(OptionalLong.empty()));
        command(
            (header, observer) -> getService().lock(LockRequest.newBuilder()
                .setHeader(header)
                .setTimeout(com.google.protobuf.Duration.newBuilder()
                    .setSeconds(timeout.getSeconds())
                    .setNanos(timeout.getNano())
                    .build())
                .build(), observer), LockResponse::getHeader)
            .thenApply(response -> response.getVersion() > 0 ? OptionalLong.of(response.getVersion()) : OptionalLong.empty())
            .thenAccept(version -> {
                timer.cancel();
                if (!future.isDone()) {
                    if (version.isPresent()) {
                        lockId.set(version.getAsLong());
                    }
                    future.complete(version);
                }
            })
            .whenComplete((result, error) -> {
                if (error != null) {
                    future.completeExceptionally(error);
                }
            });
        return future;
    }

    @Override
    public CompletableFuture<Void> unlock() {
        // Use the current lock ID to ensure we only unlock the lock currently held by this process.
        long lock = this.lockId.getAndSet(0);
        if (lock != 0) {
            return command(
                (header, observer) -> getService().unlock(UnlockRequest.newBuilder()
                    .setHeader(header)
                    .setVersion(lock)
                    .build(), observer), UnlockResponse::getHeader)
                .thenApply(response -> null);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> unlock(long version) {
        return command(
            (header, observer) -> getService().unlock(UnlockRequest.newBuilder()
                .setHeader(header)
                .setVersion(version)
                .build(), observer), UnlockResponse::getHeader)
            .thenApply(response -> response.getUnlocked());
    }

    @Override
    public CompletableFuture<Boolean> isLocked() {
        return isLocked(0);
    }

    @Override
    public CompletableFuture<Boolean> isLocked(long version) {
        return query(
            (header, observer) -> getService().isLocked(IsLockedRequest.newBuilder()
                .setHeader(header)
                .setVersion(version)
                .build(), observer), IsLockedResponse::getHeader)
            .thenApply(response -> response.getIsLocked());
    }


    @Override
    protected CompletableFuture<Void> create() {
        return this.<CreateResponse>session((header, observer) -> getService().create(CreateRequest.newBuilder()
            .build(), observer))
            .thenApply(v -> null);
    }

    @Override
    protected CompletableFuture<Void> close(boolean delete) {
        return this.<CloseResponse>session((header, observer) -> getService().close(CloseRequest.newBuilder()
            .setDelete(delete)
            .build(), observer))
            .thenApply(v -> null);
    }

    @Override
    public AtomicLock sync(Duration operationTimeout) {
        return new BlockingAtomicLock(this, operationTimeout.toMillis());
    }
}