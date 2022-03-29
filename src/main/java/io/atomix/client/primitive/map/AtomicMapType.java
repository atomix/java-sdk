// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.map;

import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.PrimitiveManagementService;
import io.atomix.client.PrimitiveType;
import io.atomix.client.primitive.map.impl.DefaultAtomicMapBuilder;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Consistent map primitive type.
 */
public class AtomicMapType<K, V> implements PrimitiveType<AtomicMapBuilder<K, V>, AtomicMap<K, V>> {
    private static final AtomicMapType INSTANCE = new AtomicMapType();

    /**
     * Returns a new consistent map type.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a new consistent map type
     */
    @SuppressWarnings("unchecked")
    public static <K, V> AtomicMapType<K, V> instance() {
        return INSTANCE;
    }

    @Override
    public AtomicMapBuilder<K, V> newBuilder(PrimitiveId id, PrimitiveManagementService managementService) {
        return new DefaultAtomicMapBuilder<>(id, managementService);
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }
}
