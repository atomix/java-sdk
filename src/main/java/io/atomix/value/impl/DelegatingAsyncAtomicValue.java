// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.value.impl;

import io.atomix.Cancellable;
import io.atomix.DelegatingAsyncPrimitive;
import io.atomix.election.LeaderElection;
import io.atomix.election.impl.BlockingLeaderElection;
import io.atomix.time.Versioned;
import io.atomix.value.AsyncAtomicValue;
import io.atomix.value.AtomicValue;
import io.atomix.value.AtomicValueEventListener;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Delegating asynchronous atomic value.
 */
public class DelegatingAsyncAtomicValue<V>
        extends DelegatingAsyncPrimitive<AsyncAtomicValue<V>, AtomicValue<V>, AsyncAtomicValue<V>>
        implements AsyncAtomicValue<V> {

    public DelegatingAsyncAtomicValue(AsyncAtomicValue<V> primitive) {
        super(primitive);
    }

    @Override
    public CompletableFuture<Versioned<V>> get() {
        return delegate().get();
    }

    @Override
    public CompletableFuture<Versioned<V>> set(V value) {
        return delegate().set(value);
    }

    @Override
    public CompletableFuture<Versioned<V>> set(V value, long version) {
        return delegate().set(value, version);
    }

    @Override
    public CompletableFuture<Cancellable> listen(AtomicValueEventListener<V> listener, Executor executor) {
        return delegate().listen(listener, executor);
    }

    @Override
    public AtomicValue<V> sync(Duration operationTimeout) {
        return new BlockingAtomicValue<>(this, operationTimeout);
    }
}
