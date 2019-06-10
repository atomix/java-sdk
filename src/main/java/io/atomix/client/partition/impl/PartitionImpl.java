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

import io.atomix.client.channel.ChannelFactory;
import io.atomix.client.partition.Partition;

/**
 * Partition implementation.
 */
public class PartitionImpl implements Partition {
    private final io.atomix.api.controller.Partition partition;
    private final ChannelFactory channelFactory;

    public PartitionImpl(io.atomix.api.controller.Partition partition) {
        this.partition = partition;
        this.channelFactory = new PartitionChannelFactory(partition);
    }

    @Override
    public int id() {
        return partition.getPartitionId();
    }

    @Override
    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }
}
