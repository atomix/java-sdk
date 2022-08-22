// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.atomix.client.counter.AtomicCounterBuilder;
import io.atomix.client.counter.impl.DefaultAtomicCounterBuilder;
import io.atomix.client.map.AtomicMapBuilder;
import io.atomix.client.map.impl.DefaultAtomicMapBuilder;
import io.atomix.client.set.DistributedSetBuilder;
import io.atomix.client.set.impl.DefaultDistributedSetBuilder;
import io.atomix.client.utils.concurrent.Threads;
import io.atomix.client.value.AtomicValueBuilder;
import io.atomix.client.value.impl.DefaultAtomicValueBuilder;
import io.grpc.ManagedChannel;
import io.grpc.NameResolverRegistry;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class AtomixClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5678;

    private final ManagedChannel channel;
    private final ScheduledExecutorService executorService;

    static {
        NameResolverRegistry.getDefaultRegistry()
            .register(new DnsNameResolverProvider());
    }

    public AtomixClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public AtomixClient(int port) {
        this(DEFAULT_HOST, port);
    }

    public AtomixClient(String host, int port) {
        this(buildChannel(host, port));
    }

    public AtomixClient(ManagedChannel channel) {
        this(channel, Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors() * 2,
            Threads.namedThreads("atomix-client-%d", LOGGER)));
    }

    private AtomixClient(ManagedChannel channel, ScheduledExecutorService executorService) {
        this.channel = channel;
        this.executorService = executorService;
    }

    private static ManagedChannel buildChannel(String host, int port) {
        return NettyChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .enableRetry()
            .nameResolverFactory(new DnsNameResolverProvider())
            .defaultLoadBalancingPolicy(new PickFirstLoadBalancerProvider().getPolicyName())
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
        return new DefaultAtomicCounterBuilder(name, channel, executorService);
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
        return new DefaultAtomicMapBuilder<>(name, channel, executorService);
    }

    /**
     * Creates a new named {@link io.atomix.client.value.AtomicValue} builder.
     * <p>
     * The value name must be provided when constructing the builder. The name is used to reference a distinct instance of
     * the primitive within the cluster. Multiple instances of the primitive with the same name will share the same state.
     * However, the instance of the primitive constructed by the returned builder will be distinct and will not share
     * local memory (e.g. cache) with any other instance on this node.
     * <p>
     * To get an asynchronous instance of the value, use the {@link SyncPrimitive#async()} method:
     * <pre>
     *   {@code
     *   AsyncAtomicValue<String> value = atomix.<String>atomicValueBuilder("my-value").build().async();
     *   }
     * </pre>
     *
     * @param name the primitive name
     * @param <V>  atomic value type
     * @return atomic value builder
     */
    public <V> AtomicValueBuilder<V> atomicValueBuilder(String name) {
        return new DefaultAtomicValueBuilder<>(name, channel, executorService);
    }

    /**
     * Creates a new named {@link io.atomix.client.set.DistributedSet} builder.
     * <p>
     * The set name must be provided when constructing the builder. The name is used to reference a distinct instance of
     * the primitive within the cluster. Multiple instances of the primitive with the same name will share the same state.
     * However, the instance of the primitive constructed by the returned builder will be distinct and will not share
     * local memory (e.g. cache) with any other instance on this node.
     * <p>
     * To get an asynchronous instance of the set, use the {@link SyncPrimitive#async()} method:
     * <pre>
     *   {@code
     *   AsyncDistributedSet<String> set = atomix.<String>setBuilder("my-set").build().async();
     *   }
     * </pre>
     *
     * @param name the primitive name
     * @param <E>  set element type
     * @return builder for an distributed set
     */
    public <E> DistributedSetBuilder<E> setBuilder(String name) {
        return new DefaultDistributedSetBuilder<>(name, channel, executorService);
    }

    /**
     * Closes the Atomix client.
     *
     * @return a completable future to be completed once the client is closed
     */
    public CompletableFuture<Void> close() {
        channel.shutdown();
        executorService.shutdown();
        return CompletableFuture.completedFuture(null);
    }
}
