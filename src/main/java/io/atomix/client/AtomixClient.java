package io.atomix.client;

import io.atomix.client.counter.AtomicCounterBuilder;
import io.atomix.client.counter.impl.DefaultAtomicCounterBuilder;
import io.atomix.client.grpc.ServiceConfigBuilder;
import io.atomix.client.map.AtomicMapBuilder;
import io.atomix.client.map.impl.DefaultAtomicMapBuilder;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.CompletableFuture;

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

    /**
     * Creates a new named {@link io.atomix.client.counter.AtomicCounter} builder.
     * <p>
     * The counter name must be provided when constructing the builder. The name is used to reference a distinct instance of
     * the primitive within the cluster. Multiple instances of the primitive with the same name will share the same state.
     * However, the instance of the primitive constructed by the returned builder will be distinct and will not share
     * local memory (e.g. cache) with any other instance on this node.
     * <p>
     * To get an asynchronous instance of the counter, use the {@link SyncPrimitive#async()} method:
     * <pre>
     *   {@code
     *   AsyncAtomicCounter counter = atomix.atomicCounterBuilder("my-counter").build().async();
     *   }
     * </pre>
     *
     * @param name the primitive name
     * @return atomic counter builder
     */
    public AtomicCounterBuilder atomicCounterBuilder(String name) {
        return new DefaultAtomicCounterBuilder(name, channel);
    }

    /**
     * Creates a new named {@link io.atomix.client.map.AtomicMap} builder.
     * <p>
     * The map name must be provided when constructing the builder. The name is used to reference a distinct instance of
     * the primitive within the cluster. Multiple instances of the primitive with the same name will share the same state.
     * However, the instance of the primitive constructed by the returned builder will be distinct and will not share
     * local memory (e.g. cache) with any other instance on this node.
     * <p>
     * To get an asynchronous instance of the map, use the {@link SyncPrimitive#async()} method:
     * <pre>
     *   {@code
     *   AsyncAtomicMap<String, String> map = atomix.<String, String>atomicMapBuilder("my-map").build().async();
     *   }
     * </pre>
     *
     * @param name the primitive name
     * @param <K>  key type
     * @param <V>  value type
     * @return builder for a atomic map
     */
    public <K, V> AtomicMapBuilder<K, V> atomicMapBuilder(String name) {
        return new DefaultAtomicMapBuilder<>(name, channel);
    }

    /**
     * Closes the Atomix client.
     *
     * @return a completable future to be completed once the client is closed
     */
    public CompletableFuture<Void> close() {
        channel.shutdown();
        return CompletableFuture.completedFuture(null);
    }
}
