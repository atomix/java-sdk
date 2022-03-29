// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.map;

import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.PrimitiveManagementService;

/**
 * Builder for {@link AtomicMap} instances.
 *
 * @param <K> type for map key
 * @param <V> type for map value
 */
public abstract class AtomicMapBuilder<K, V>
        extends MapBuilder<AtomicMapBuilder<K, V>, AtomicMap<K, V>, K, V> {
    protected AtomicMapBuilder(PrimitiveId primitiveId, PrimitiveManagementService managementService) {
        super(primitiveId, managementService);
    }
}
