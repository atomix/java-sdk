// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.grpc;

import atomix.counter.v1.CounterGrpc;
import atomix.election.v1.LeaderElectionGrpc;

/**
 * Enum for the primitive services to be used in the service config builder.
 */
public enum PrimitiveService {
    /**
     * Counter gRPC service.
     */
    COUNTER(CounterGrpc.SERVICE_NAME),
    /**
     * Election gRPC service
     */
    ELECTION(LeaderElectionGrpc.SERVICE_NAME);

    private final String serviceName;

    PrimitiveService(String value) {
        serviceName = value;
    }

    public String getServiceName() {
        return serviceName;
    }
}
