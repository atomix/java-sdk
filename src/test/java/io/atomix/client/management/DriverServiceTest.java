// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.management;

import com.google.common.collect.Maps;
import io.atomix.api.management.broker.PrimitiveAddress;
import io.atomix.api.primitive.CreateRequest;
import io.atomix.api.primitive.CreateResponse;
import io.atomix.api.primitive.PrimitiveGrpc;
import io.atomix.api.primitive.RequestHeaders;
import io.atomix.api.primitive.ResponseHeaders;
import io.atomix.api.primitive.meta.LogicalTimestamp;
import io.atomix.api.primitive.meta.Timestamp;
import io.atomix.client.management.driver.DriverService;
import io.atomix.client.utils.channel.ChannelFactory;
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

import static io.atomix.client.utils.concurrent.Futures.futureGetOrElse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

/**
 * Tests for Driver service
 */
public class DriverServiceTest {

    private final static String SERVICE_NAME = "pippo";
    private final static int SERVICE_PORT = 12345;

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final TestDriverService driverService = new TestDriverService();
    private final PrimitiveAddress primitiveAddress = PrimitiveAddress.newBuilder()
            .setHost(SERVICE_NAME)
            .setPort(SERVICE_PORT)
            .build();
    private final long currentTime = System.currentTimeMillis();
    private final CreateRequest createRequest = CreateRequest.newBuilder()
            .setHeaders(RequestHeaders.newBuilder().setTimestamp(Timestamp.newBuilder().setLogicalTimestamp(
                    LogicalTimestamp.newBuilder().setTime(currentTime).build()).build()).build())
            .build();

    @Before
    public void setUp() throws Exception {
        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(SERVICE_NAME).directExecutor().addService(serviceImpl).build().start());
    }

    @Test
    public void testGetDriverChannel() {
        assertTrue(driverService.drivers.isEmpty());

        Channel channel = driverService.getDriverChannel(primitiveAddress);
        assertNotNull(channel);
        assertEquals(1, driverService.drivers.size());

        PrimitiveGrpc.PrimitiveStub client = PrimitiveGrpc.newStub(channel);
        client.create(createRequest, streamObserver);
        Timestamp currentTs = futureGetOrElse(future, null);

        assertNotNull(currentTs);
        assertEquals(currentTime, currentTs.getLogicalTimestamp().getTime());

        channel = driverService.getDriverChannel(primitiveAddress);
        assertNotNull(channel);
        assertEquals(1, driverService.drivers.size());
    }

    // Mock implementation of the server, returns the provided ts
    private final PrimitiveGrpc.PrimitiveImplBase serviceImpl = mock(PrimitiveGrpc.PrimitiveImplBase.class, delegatesTo(
            new PrimitiveGrpc.PrimitiveImplBase() {
                @Override
                public void create(CreateRequest request,
                                   StreamObserver<CreateResponse> respObserver) {
                    long ts = request.getHeaders().getTimestamp().getLogicalTimestamp().getTime();
                    ResponseHeaders responseHeaders = ResponseHeaders.newBuilder()
                            .setTimestamp(Timestamp.newBuilder().setLogicalTimestamp(LogicalTimestamp.newBuilder().setTime(ts).build()).build())
                            .build();
                    respObserver.onNext(CreateResponse.newBuilder().setHeaders(responseHeaders).build());
                    respObserver.onCompleted();
                }
            })
        );

    private final CompletableFuture<Timestamp> future = new CompletableFuture<>();
    private final StreamObserver<CreateResponse> streamObserver = new StreamObserver<>() {
        @Override
        public void onNext(CreateResponse createResponse) {
            future.complete(createResponse.getHeaders().getTimestamp());
        }

        @Override
        public void onError(Throwable throwable) {
            future.completeExceptionally(throwable);
        }

        @Override
        public void onCompleted() {

        }
    };

    private class TestDriverService implements DriverService {
        public final Map<PrimitiveAddress, ChannelFactory> drivers = Maps.newConcurrentMap();

        @Override
        public Channel getDriverChannel(PrimitiveAddress primitiveAddress) {
            return drivers.computeIfAbsent(primitiveAddress, driver -> new TestChannelFactory()).getChannel();
        }
    }

    private class TestChannelFactory implements ChannelFactory {

        @Override
        public Channel getChannel() {
            return grpcCleanup.register(InProcessChannelBuilder.forName(SERVICE_NAME).directExecutor().build());
        }
    }

}
