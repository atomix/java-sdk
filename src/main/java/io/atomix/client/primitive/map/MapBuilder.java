// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.map;

import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.PrimitiveBuilder;
import io.atomix.client.PrimitiveManagementService;
import io.atomix.client.SyncPrimitive;

/**
 * Base map builder.
 */
public abstract class MapBuilder<B extends MapBuilder<B, P, K, V>, P extends SyncPrimitive, K, V>
        extends PrimitiveBuilder<B, P> {
    protected MapBuilder(PrimitiveId primitiveId, PrimitiveManagementService managementService) {
        super(primitiveId, managementService);
    }
}
