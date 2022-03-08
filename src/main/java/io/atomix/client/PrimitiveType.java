// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;


import io.atomix.api.primitive.PrimitiveId;

/**
 * Primitive type.
 */
public interface PrimitiveType<B extends PrimitiveBuilder, P extends SyncPrimitive> {

    /**
     * Returns a new primitive builder.
     *
     * @param primitiveId the primitive ID
     * @param managementService the primitive management service
     * @return a new primitive builder
     */
    B newBuilder(PrimitiveId primitiveId, PrimitiveManagementService managementService);

}
