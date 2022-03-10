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
package io.atomix.client;

/**
 * Manages the creation of distributed primitive instances.
 * <p>
 * TODO add additional documentation
 */
public interface PrimitiveFactory {

    /**
     * Creates a new named primitive builder of the given {@code primitiveType}.
     * <p>
     * The primitive name must be provided when constructing the builder. The name is used to reference a distinct instance of
     * the primitive within the cluster. Multiple instances of the primitive with the same name will share the same state.
     * However, the instance of the primitive constructed by the returned builder will be distinct and will not share
     * local memory (e.g. cache) with any other instance on this node.
     * <p>
     * To get an asynchronous instance of the primitive, use the {@link SyncPrimitive#async()} method:
     * <pre>
     *   {@code
     *   AsyncPrimitive async = atomix.primitiveBuilder("my-primitive", MyPrimitiveType.instance()).build().async();
     *   }
     * </pre>
     *
     * @param name          the primitive name
     * @param primitiveType the primitive type
     * @param <B>           the primitive builder type
     * @param <P>           the primitive type
     * @return the primitive builder
     */
    <B extends PrimitiveBuilder<B, P>, P extends SyncPrimitive> B primitiveBuilder(
            String name,
            PrimitiveType<B, P> primitiveType);

}
