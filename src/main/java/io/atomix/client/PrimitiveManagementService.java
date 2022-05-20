// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Primitive management service
 */
public interface PrimitiveManagementService {
    /**
     * Gets or creates a locally cached multiton primitive instance.
     *
     * @param primitiveName the primitive name
     * @param supplier the primitive factory
     * @param <P>      the primitive type
     * @return the primitive instance
     */
    <P extends DistributedPrimitive> CompletableFuture<P> getPrimitive(String primitiveName, Supplier<CompletableFuture<P>> supplier);
}
