// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;


import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.utils.concurrent.ThreadContextFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Primitive management service. Provides access to the broker sidecar;
 */
public interface PrimitiveManagementService {

    /**
     * Returns the thread context factory.
     *
     * @return the thread context factory
     */
    ThreadContextFactory getThreadFactory();

    /**
     * Gets or creates a locally cached multiton primitive instance.
     * An instance is defacto a gRPC connection with the driver.
     *
     * @param primitiveId the primitive id
     * @param supplier the primitive factory
     * @param <P> the primitive type
     * @return the primitive instance
     */
    <P extends DistributedPrimitive> CompletableFuture<P> getPrimitive(PrimitiveId primitiveId,
                                                                       Supplier<CompletableFuture<P>> supplier);

}
