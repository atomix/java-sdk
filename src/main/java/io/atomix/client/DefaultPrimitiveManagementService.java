// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.atomix.api.management.broker.PrimitiveAddress;
import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.management.broker.BrokerService;
import io.atomix.client.management.driver.DriverService;
import io.atomix.client.utils.concurrent.ThreadContextFactory;
import io.grpc.Channel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Default implementation of the primitive management service.
 */
public class DefaultPrimitiveManagementService implements PrimitiveManagementService {

    private final BrokerService brokerService;
    private final ThreadContextFactory threadContextFactory;
    private final DriverService driverService;

    public DefaultPrimitiveManagementService(BrokerService bService, DriverService dService,
                                             ThreadContextFactory tFactory) {
        brokerService = bService;
        threadContextFactory = tFactory;
        driverService = dService;
    }

    @Override
    public ThreadContextFactory getThreadFactory() {
        return threadContextFactory;
    }

    @Override
    public <P extends DistributedPrimitive> CompletableFuture<P> getPrimitive(PrimitiveId primitiveId,
                                                                              Supplier<CompletableFuture<P>> supplier) {
        PrimitiveAddress primitiveAddress = brokerService.lookupPrimitive(primitiveId);
        Channel primitiveChannel = driverService.getDriverChannel(primitiveAddress);
        // TODO craft the equivalent of the proxyClient
        return null;
    }

}
