package io.atomix.client;

import io.atomix.client.grpc.ServiceConfigBuilder;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

public final class AtomixClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5678;

    private final ManagedChannel channel;

    public AtomixClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public AtomixClient(int port) {
        this(DEFAULT_HOST, port);
    }

    public AtomixClient(String host, int port) {
        channel = NettyChannelBuilder.forAddress(host, port)
                .enableRetry()
                .defaultServiceConfig(ServiceConfigBuilder.DEFAULT_SERVICE_CONFIG)
                .build();
    }

    private <B extends PrimitiveBuilder<B, P>, P extends SyncPrimitive> B primitiveBuilder(
            String name,
            PrimitiveType<B, P> primitiveType) {
        return primitiveType.newBuilder(name, channel);
    }
}
