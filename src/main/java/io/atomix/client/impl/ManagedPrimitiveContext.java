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
package io.atomix.client.impl;

import java.time.Duration;

import io.atomix.client.PrimitiveType;

/**
 * Managed primitive context.
 */
public class ManagedPrimitiveContext {
    private final long sessionId;
    private final String name;
    private final PrimitiveType type;
    private final Duration timeout;

    public ManagedPrimitiveContext(long sessionId, String name, PrimitiveType type, Duration timeout) {
        this.sessionId = sessionId;
        this.name = name;
        this.type = type;
        this.timeout = timeout;
    }

    /**
     * Returns the primitive session ID.
     *
     * @return the primitive session ID
     */
    public long sessionId() {
        return sessionId;
    }

    /**
     * Returns the primitive name.
     *
     * @return the primitive name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the primitive type.
     *
     * @return the primitive type
     */
    public PrimitiveType type() {
        return type;
    }

    /**
     * Returns the session timeout.
     *
     * @return the session timeout
     */
    public Duration timeout() {
        return timeout;
    }
}