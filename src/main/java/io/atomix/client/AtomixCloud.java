// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.atomix.api.primitive.PrimitiveId;
import io.atomix.client.management.broker.impl.DefaultBroker;
import io.atomix.client.management.driver.impl.DefaultDriverService;
import io.atomix.client.utils.channel.ChannelConfig;
import io.atomix.client.utils.channel.ChannelProvider;
import io.atomix.client.utils.concurrent.BlockingAwareThreadPoolContextFactory;
import io.atomix.client.utils.concurrent.ThreadContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

/**
 * Primary interface for managing Atomix clusters and operating on distributed primitives.
 * <p>
 * TODO add additional documentation
 */
public class AtomixCloud implements AtomixCloudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixCloud.class);

    private final String namespace;
    private final ChannelProvider brokerProvider;
    private final ChannelConfig driverConfig;
    private ThreadContextFactory threadContextFactory;
    private final AtomicBoolean started = new AtomicBoolean();
    private PrimitiveManagementService managementService;

    protected AtomixCloud(String namespace, ChannelProvider bProvider, ChannelConfig dConfig) {
        this.namespace = namespace;
        this.brokerProvider = bProvider;
        this.driverConfig = dConfig;
    }

    @Override
    public ThreadContextFactory getThreadFactory() {
        return threadContextFactory;
    }

    private PrimitiveId getPrimitiveId(String name) {
        return PrimitiveId.newBuilder()
            .setName(name)
            .setNamespace(namespace)
            .build();
    }

    @Override
    public <B extends PrimitiveBuilder<B, P>, P extends SyncPrimitive> B primitiveBuilder(
        String name,
        PrimitiveType<B, P> primitiveType) {
        checkRunning();
        return primitiveType.newBuilder(getPrimitiveId(name), managementService);
    }

    /**
     * Checks that the instance is running.
     */
    private void checkRunning() {
        checkState(isRunning(), "Atomix instance is not running");
    }

    /**
     * Starts the Atomix instance.
     * <p>
     * The returned future will be completed once this instance completes startup. Note that in order to complete startup,
     * all partitions must be able to form. For Raft partitions, that requires that a majority of the nodes in each
     * partition be started concurrently.
     *
     * @return a future to be completed once the instance has completed startup
     */
    public synchronized CompletableFuture<AtomixCloud> start() {
        this.threadContextFactory = new BlockingAwareThreadPoolContextFactory(
            "atomix-client-%d",
            Runtime.getRuntime().availableProcessors(),
            LOGGER);
        this.managementService = new DefaultPrimitiveManagementService(
                new DefaultBroker(brokerProvider.getFactory()),
                new DefaultDriverService(driverConfig),
                threadContextFactory);
        started.set(true);
        LOGGER.info("Started");
        return CompletableFuture.completedFuture(this);
    }

    /**
     * Returns a boolean indicating whether the instance is running.
     *
     * @return indicates whether the instance is running
     */
    public boolean isRunning() {
        return started.get();
    }

    /**
     * Stops the instance.
     *
     * @return a future to be completed once the instance has been stopped
     */
    public synchronized CompletableFuture<Void> stop() {
        threadContextFactory.close();
        LOGGER.info("Stopped");
        started.set(false);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

    /**
     * Returns a new AtomixCloud builder.
     *
     * @return a new AtomixCloud builder
     */
    public static AtomixCloudBuilder builder() {
        return new AtomixCloudBuilder();
    }

}
