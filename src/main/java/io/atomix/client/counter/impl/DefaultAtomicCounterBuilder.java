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
package io.atomix.client.counter.impl;

import io.atomix.api.primitive.Name;
import io.atomix.client.AsyncAtomixClient;
import io.atomix.client.PrimitiveManagementService;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.counter.AtomicCounterBuilder;
import io.atomix.client.session.Session;
import io.atomix.client.utils.concurrent.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Atomic counter proxy builder.
 */
public class DefaultAtomicCounterBuilder extends AtomicCounterBuilder {
    public DefaultAtomicCounterBuilder(Name name, PrimitiveManagementService managementService) {
        super(name, managementService);
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAtomicCounterBuilder.class);

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<AtomicCounter> buildAsync() {
        LOGGER.info("Atomic Counter build");
        Session session = managementService.getSessionService().getSession(partitioner.partition(getName().getName(), managementService.getPartitionService().getPartitionIds()));
        LOGGER.info("Session Info" + session.toString());
        ThreadContext context = managementService.getThreadFactory().createContext();
        LOGGER.info("Context info" + context.toString());
        return new DefaultAsyncAtomicCounter(
            getName(),
            session,
            context)
            .connect()
            .thenApply(AsyncAtomicCounter::sync);
    }
}
