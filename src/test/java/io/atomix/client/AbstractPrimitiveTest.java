// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.grpc.BindableService;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;

/**
 * Base class for the tests.
 */
public abstract class AbstractPrimitiveTest {

    protected BindableService serviceImpl;
    protected Channel channel;

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                                     .directExecutor()
                                     .addService(serviceImpl)
                                     .build()
                                     .start());

        // Create a client channel and register for automatic graceful shutdown.
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName)
                                                       .directExecutor()
                                                       .build());
    }
}
