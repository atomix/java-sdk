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
package io.atomix.client.partition.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.atomix.api.controller.ControllerServiceGrpc;
import io.atomix.api.controller.GetPartitionGroupsRequest;
import io.atomix.api.controller.GetPartitionGroupsResponse;
import io.atomix.api.controller.PartitionGroupId;
import io.atomix.client.channel.ChannelFactory;
import io.atomix.client.partition.PartitionGroup;
import io.atomix.client.partition.PartitionService;
import io.grpc.stub.StreamObserver;

/**
 * Partition service implementation.
 */
public class PartitionServiceImpl implements PartitionService {
    private final ControllerServiceGrpc.ControllerServiceStub service;

    public PartitionServiceImpl(ChannelFactory channelFactory) {
        this.service = ControllerServiceGrpc.newStub(channelFactory.getChannel());
    }

    @Override
    public CompletableFuture<PartitionGroup> getPartitionGroup(PartitionGroupId id) {
        return this.<GetPartitionGroupsResponse>execute(observer ->
            service.getPartitionGroups(GetPartitionGroupsRequest.newBuilder()
                .setId(id)
                .build(), observer))
            .thenApply(response -> {
                if (response.getGroupsList().isEmpty()) {
                    return null;
                }
                return new PartitionGroupImpl(response.getGroups(0));
            });
    }

    private <T> CompletableFuture<T> execute(Consumer<StreamObserver<T>> callback) {
        CompletableFuture<T> future = new CompletableFuture<>();
        callback.accept(new StreamObserver<T>() {
            @Override
            public void onNext(T value) {
                future.complete(value);
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
}
