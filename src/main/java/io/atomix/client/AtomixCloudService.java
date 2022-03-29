// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.atomix.client.primitive.map.AtomicMapBuilder;
import io.atomix.client.primitive.map.AtomicMapType;
import io.atomix.client.utils.concurrent.ThreadContextFactory;

/**
 * Manages the creation of distributed primitive instances.
 * <p>
 * TODO add additional documentation
 */
public interface AtomixCloudService extends PrimitiveFactory {

    /**
     * Returns the Atomix primitive thread factory.
     *
     * @return the primitive thread context factory
     */
    ThreadContextFactory getThreadFactory();

    /**
     * Creates a new named {@link io.atomix.client.primitive.map.AtomicMap} builder.
     * <p>
     * The map name must be provided when constructing the builder. The name is used to reference a distinct instance of
     * the primitive within the cluster. Multiple instances of the primitive with the same name will share the same state.
     * However, the instance of the primitive constructed by the returned builder will be distinct and will not share
     * local memory (e.g. cache) with any other instance on this node.
     * <p>
     * To get an asynchronous instance of the map, use the {@link SyncPrimitive#async()} method:
     * <pre>
     *   {@code
     *   AsyncAtomicMap<String, String> map = atomix.<String, String>atomicMapBuilder("my-map").build().async();
     *   }
     * </pre>
     *
     * @param name the primitive name
     * @param <K>  key type
     * @param <V>  value type
     * @return builder for a atomic map
     */
    default <K, V> AtomicMapBuilder<K, V> atomicMapBuilder(String name) {
        return primitiveBuilder(name, AtomicMapType.instance());
    }

}
