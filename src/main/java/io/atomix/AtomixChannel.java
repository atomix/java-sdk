package io.atomix;

import io.atomix.utils.concurrent.Threads;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolverRegistry;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * gRPC {@link io.grpc.Channel} for Atomix primitives.
 */
public class AtomixChannel extends ManagedChannel {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixChannel.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5678;

    static {
        NameResolverRegistry.getDefaultRegistry()
            .register(new DnsNameResolverProvider());
    }

    private final ManagedChannel parent;
    private final ScheduledExecutorService executorService;

    public AtomixChannel() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public AtomixChannel(int port) {
        this(DEFAULT_HOST, port);
    }

    public AtomixChannel(String host, int port) {
        this(buildChannel(host, port));
    }

    public AtomixChannel(ManagedChannel channel) {
        this(channel, Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors() * 2,
            Threads.namedThreads("atomix-channel-%d", LOGGER)));
    }

    private AtomixChannel(ManagedChannel parent, ScheduledExecutorService executorService) {
        this.parent = parent;
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

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
        return parent.newCall(methodDescriptor, callOptions);
    }

    @Override
    public String authority() {
        return parent.authority();
    }

    public ScheduledExecutorService executor() {
        return executorService;
    }

    @Override
    public ManagedChannel shutdown() {
        parent.shutdown();
        executorService.shutdown();
        return this;
    }

    @Override
    public boolean isShutdown() {
        return parent.isShutdown() && executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return parent.isTerminated() && executorService.isTerminated();
    }

    @Override
    public ManagedChannel shutdownNow() {
        parent.shutdownNow();
        executorService.shutdownNow();
        return this;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return parent.awaitTermination(timeout, timeUnit) && executorService.awaitTermination(timeout, timeUnit);
    }
}
