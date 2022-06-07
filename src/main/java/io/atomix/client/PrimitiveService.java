// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import atomix.counter.v1.CounterGrpc;
import atomix.election.v1.LeaderElectionGrpc;

/**
 * Enum for the primitive services to be used in the service config builder.
 */
public enum PrimitiveService {
    /**
     * Cunter gRPC service.
     */
    COUNTER(CounterGrpc.SERVICE_NAME),
    ELECTION(LeaderElectionGrpc.SERVICE_NAME);

    private final String serviceName;

    PrimitiveService(String value) {
        serviceName = value;
    }

    public String getServiceName() {
        return serviceName;
    }
}
