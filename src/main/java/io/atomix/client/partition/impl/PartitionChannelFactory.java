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

import io.atomix.api.controller.Partition;
import io.atomix.api.controller.PartitionEndpoint;
import io.atomix.client.channel.ChannelFactory;
import io.grpc.Channel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/**
 * Partition group channel factory.
 */
public class PartitionChannelFactory implements ChannelFactory {
    private final Partition partition;
    private final NettyChannelBuilder builder;

    public PartitionChannelFactory(Partition partition) {
        this.partition = partition;
        PartitionEndpoint endpoint = partition.getEndpoints(0);
        if (endpoint.getPort() != 0) {
            this.builder = NettyChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPort()).usePlaintext();
        } else {
            this.builder = NettyChannelBuilder.forTarget(endpoint.getHost()).usePlaintext();
        }
    }

    @Override
    public Channel getChannel() {
        return builder.build();
    }
}
