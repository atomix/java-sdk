/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client;

import io.atomix.api.controller.*;
import io.atomix.client.channel.ChannelProvider;
import io.atomix.client.impl.DefaultPrimitiveManagementService;
import io.atomix.client.impl.PrimitiveCacheImpl;
import io.atomix.client.partition.PartitionService;
import io.atomix.client.partition.impl.PartitionServiceImpl;
import io.atomix.client.session.SessionService;
import io.atomix.client.utils.concurrent.BlockingAwareThreadPoolContextFactory;
import io.atomix.client.utils.concurrent.Futures;
import io.atomix.client.utils.concurrent.ThreadContextFactory;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Primary interface for managing Atomix clusters and operating on distributed primitives.
 */
public class AsyncAtomixClient {

    /**
     * Returns a new Atomix client builder.
     *
     * @return a new Atomix builder
     */
    public static AtomixClientBuilder builder() {
        return new AtomixClientBuilder();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncAtomixClient.class);
    private static final Duration SESSION_TIMEOUT = Duration.ofSeconds(30);

    private final String namespace;
    private final ChannelProvider channelProvider;
    private final PrimitiveCache primitiveCache = new PrimitiveCacheImpl();
    private ThreadContextFactory threadContextFactory;
    private final AtomicBoolean started = new AtomicBoolean();

    protected AsyncAtomixClient(String namespace, ChannelProvider channelProvider) {
        this.threadContextFactory =  new BlockingAwareThreadPoolContextFactory(
                "atomix-client-%d",
                Runtime.getRuntime().availableProcessors(),
                LOGGER);
        this.namespace = namespace;
        this.channelProvider = channelProvider;
    }

    /**
     * Returns a list of databases in the cluster.
     *
     * @return a list of databases supported by the controller
     */
    public CompletableFuture<Collection<AtomixDatabase>> getDatabases() {
        Channel channel = channelProvider.getFactory().getChannel();
        ControllerServiceGrpc.ControllerServiceStub controller = ControllerServiceGrpc.newStub(channel);
        CompletableFuture<Collection<AtomixDatabase>> future = new CompletableFuture<>();
        controller.getDatabases(GetDatabasesRequest.newBuilder()
            .setId(DatabaseId.newBuilder()
                .setNamespace(namespace)
                .build())
            .build(), new StreamObserver<>() {
            @Override
            public void onNext(GetDatabasesResponse response) {
                if (response.getDatabasesCount() == 0) {
                    future.complete(null);
                } else {
                    Futures.allOf(response.getDatabasesList().stream()
                        .map(AsyncAtomixClient.this::createDatabase)
                        .collect(Collectors.toList())).whenComplete((databases, error) -> {
                        if (error != null) {
                            future.completeExceptionally(error);
                        } else {
                            future.complete(databases);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {

            }
        });
        return future;
    }

    /**
     * Returns a database by name.
     *
     * @param name the database name
     * @return the database
     */
    public CompletableFuture<AtomixDatabase> getDatabase(String name) {
        Channel channel = channelProvider.getFactory().getChannel();
        ControllerServiceGrpc.ControllerServiceStub controller = ControllerServiceGrpc.newStub(channel);
        CompletableFuture<AtomixDatabase> future = new CompletableFuture<>();
        controller.getDatabases(GetDatabasesRequest.newBuilder()
            .setId(DatabaseId.newBuilder()
                .setNamespace(namespace)
                .setName(name)
                .build())
            .build(), new StreamObserver<>() {
            @Override
            public void onNext(GetDatabasesResponse response) {
                if (response.getDatabasesCount() == 0) {
                    future.complete(null);
                } else {
                    createDatabase(response.getDatabases(0)).whenComplete((database, error) -> {
                        if (error != null) {
                            future.completeExceptionally(error);
                        } else {
                            future.complete(database);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {

            }
        });
        return future;
    }

    private CompletableFuture<AtomixDatabase> createDatabase(Database database) {
        LOGGER.info("creating new database");
        PartitionService partitionService = new PartitionServiceImpl(database);
        SessionService sessionService = new SessionService(partitionService, threadContextFactory, SESSION_TIMEOUT);
        return sessionService.connect().thenApply(v -> new AtomixDatabase(
            database.getId().getName(),
            database.getId().getNamespace(),
            new DefaultPrimitiveManagementService(
                partitionService,
                sessionService,
                primitiveCache,
                threadContextFactory)));
    }

    /**
     * Connects the client.
     *
     * @return a future to be completed once the client has connected
     */
    public synchronized CompletableFuture<AsyncAtomixClient> connect() {
        this.threadContextFactory = new BlockingAwareThreadPoolContextFactory(
            "atomix-client-%d",
            Runtime.getRuntime().availableProcessors(),
            LOGGER);
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
     * Closes the client.
     *
     * @return a future to be completed once the client has been closed
     */
    public synchronized CompletableFuture<Void> close() {
        threadContextFactory.close();
        LOGGER.info("Stopped");
        started.set(false);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }
}
