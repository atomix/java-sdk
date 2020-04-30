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

import io.atomix.api.primitive.Name;
import io.atomix.client.utils.concurrent.ThreadContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for a single database.
 */
public class AtomixDatabase implements PrimitiveClient {
    private final String name;
    private final String namespace;
    private final PrimitiveManagementService managementService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixDatabase.class);

    public AtomixDatabase(String name, String namespace, PrimitiveManagementService managementService) {
        this.name = name;
        this.namespace = namespace;
        this.managementService = managementService;
    }

    /**
     * Returns the database name.
     *
     * @return the database name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the database namespace.
     *
     * @return the database namespace
     */
    public String getNamespace() {
        return namespace;
    }

    @Override
    public ThreadContextFactory getThreadFactory() {
        return managementService.getThreadFactory();
    }

    private Name getPrimitiveName(String name) {
        return Name.newBuilder()
            .setName(name)
            .setNamespace(namespace)
            .build();
    }

    @Override
    public <B extends PrimitiveBuilder<B, P>, P extends SyncPrimitive> B primitiveBuilder(
        String name,
        PrimitiveType<B, P> primitiveType) {
        return primitiveType.newBuilder(getPrimitiveName(name), managementService);
    }
}
