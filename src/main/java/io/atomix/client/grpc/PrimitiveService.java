// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.grpc;

import io.atomix.api.runtime.atomic.counter.v1.AtomicCounterGrpc;
import io.atomix.api.runtime.atomic.map.v1.AtomicMapGrpc;

/**
 * Enum for the primitive services to be used in the service config builder.
 */
public enum PrimitiveService {
    ATOMIC_COUNTER(AtomicCounterGrpc.SERVICE_NAME),
    ATOMIC_MAP(AtomicMapGrpc.SERVICE_NAME);

    private final String serviceName;

    PrimitiveService(String value) {
        serviceName = value;
    }

    public String getServiceName() {
        return serviceName;
    }
}
