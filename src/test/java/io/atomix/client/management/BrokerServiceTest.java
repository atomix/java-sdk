// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.management;

import com.google.common.collect.Maps;
import io.atomix.api.management.broker.BrokerGrpc;
import io.atomix.api.management.broker.LookupPrimitiveRequest;
import io.atomix.api.management.broker.LookupPrimitiveResponse;
import io.atomix.api.management.broker.PrimitiveAddress;
import io.atomix.api.management.broker.PrimitiveId;
import io.atomix.api.management.broker.RegisterPrimitiveRequest;
import io.atomix.api.management.broker.RegisterPrimitiveResponse;
import io.atomix.api.management.broker.UnregisterPrimitiveRequest;
import io.atomix.api.management.broker.UnregisterPrimitiveResponse;
import io.atomix.client.management.broker.BrokerService;
import io.atomix.client.management.broker.impl.DefaultBroker;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.atomix.client.utils.concurrent.Futures.futureGetOrElse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

/**
 * Tests for the broker APIs
 */
public class BrokerServiceTest {

    private final Map<PrimitiveId, PrimitiveAddress> registeredPrimitives = Maps.newConcurrentMap();
    private final io.atomix.api.primitive.PrimitiveId primitiveId = io.atomix.api.primitive.PrimitiveId.newBuilder()
            .setType("FOO")
            .setName("BAR")
            .setNamespace("FOOBAR")
            .build();
    private final PrimitiveAddress primitiveAddress = PrimitiveAddress.newBuilder()
            .setHost("localhost")
            .setPort(10000)
            .build();

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    // For register/unregister emulation
    private BrokerGrpc.BrokerStub clientImpl;
    private BrokerService brokerService;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        Channel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        clientImpl = BrokerGrpc.newStub(channel);
        brokerService = new DefaultBroker(channel);
    }

    @Test
    public void testLookupAfterRegister() {
        assertTrue(registerPrimitive(primitiveId, primitiveAddress));

        PrimitiveAddress testPrimitiveAddress = brokerService.lookupPrimitive(primitiveId);
        assertEquals(primitiveAddress, testPrimitiveAddress);
    }

    @Test
    public void testLookupAfterUnRegister() {
        assertTrue(unregisterPrimitive(primitiveId));
        assertNull(brokerService.lookupPrimitive(primitiveId));
    }

    // Client call to register a primitive
    private boolean registerPrimitive(io.atomix.api.primitive.PrimitiveId primitiveId,
                                            PrimitiveAddress primitiveAddress) {
        return this.<RegisterPrimitiveResponse>execute(observer ->
                clientImpl.registerPrimitive(RegisterPrimitiveRequest.newBuilder()
                        .setPrimitiveId(PrimitiveId.newBuilder()
                                .setId(primitiveId)
                                .build())
                        .setAddress(primitiveAddress)
                        .build(), observer));
    }

    // Client call to unregister a primitive
    private boolean unregisterPrimitive(io.atomix.api.primitive.PrimitiveId primitiveId) {
        return this.<UnregisterPrimitiveResponse>execute(observer ->
                clientImpl.unregisterPrimitive(UnregisterPrimitiveRequest.newBuilder()
                        .setPrimitiveId(PrimitiveId.newBuilder()
                                .setId(primitiveId)
                                .build())
                        .build(), observer));
    }

    // Utility to consume stream observers
    private <T> boolean execute(Consumer<StreamObserver<T>> callback) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        callback.accept(new StreamObserver<T>() {
            @Override
            public void onNext(T value) {
                future.complete(true);
            }

            @Override
            public void onError(Throwable t) {
                future.complete(false);
            }

            @Override
            public void onCompleted() {

            }
        });
        return futureGetOrElse(future, false);
    }

    // Mock implementation of the server
    private final BrokerGrpc.BrokerImplBase serviceImpl = mock(BrokerGrpc.BrokerImplBase.class, delegatesTo(
            new BrokerGrpc.BrokerImplBase() {

                @Override
                public void registerPrimitive(RegisterPrimitiveRequest request,
                                              StreamObserver<RegisterPrimitiveResponse> respObserver) {
                    registeredPrimitives.put(request.getPrimitiveId(), request.getAddress());
                    respObserver.onNext(RegisterPrimitiveResponse.newBuilder().build());
                    respObserver.onCompleted();;
                }

                @Override
                public void unregisterPrimitive(UnregisterPrimitiveRequest request,
                                                StreamObserver<UnregisterPrimitiveResponse> respObserver) {
                    registeredPrimitives.remove(request.getPrimitiveId());
                    respObserver.onNext(UnregisterPrimitiveResponse.newBuilder().build());
                    respObserver.onCompleted();
                }

                @Override
                public void lookupPrimitive(LookupPrimitiveRequest request,
                                            StreamObserver<LookupPrimitiveResponse> responseObserver) {
                    PrimitiveAddress primitiveAddress = registeredPrimitives.get(request.getPrimitiveId());
                    if (primitiveAddress != null) {
                        responseObserver.onNext(LookupPrimitiveResponse.newBuilder()
                                .setAddress(primitiveAddress)
                                .build());
                    }
                    responseObserver.onCompleted();
                }
            })
    );

}
