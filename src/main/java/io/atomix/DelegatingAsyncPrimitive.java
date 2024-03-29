// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for primitive delegates.
 */
public abstract class DelegatingAsyncPrimitive<A extends AsyncPrimitive<A, S>, S extends SyncPrimitive<S, A>, T extends AsyncPrimitive<T, ?>> implements AsyncPrimitive<A, S> {
    private final T primitive;

    public DelegatingAsyncPrimitive(T primitive) {
        this.primitive = checkNotNull(primitive);
    }

    /**
     * Returns the delegate primitive.
     *
     * @return the underlying primitive
     */
    protected T delegate() {
        return primitive;
    }

    @Override
    public String name() {
        return primitive.name();
    }

    @Override
    public CompletableFuture<Void> close() {
        return primitive.close();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("delegate", primitive)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(primitive);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DelegatingAsyncPrimitive
                && primitive.equals(((DelegatingAsyncPrimitive) other).primitive);
    }
}