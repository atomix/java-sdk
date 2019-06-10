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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.atomix.api.controller.PartitionGroupId;
import io.atomix.client.partition.Partition;
import io.atomix.client.partition.PartitionGroup;

/**
 * Partition group implementation.
 */
public class PartitionGroupImpl implements PartitionGroup {
    private final io.atomix.api.controller.PartitionGroup group;
    private final Map<Integer, Partition> partitions = new ConcurrentHashMap<>();
    private final List<Integer> partitionIds = new CopyOnWriteArrayList<>();

    public PartitionGroupImpl(io.atomix.api.controller.PartitionGroup group) {
        this.group = group;
        group.getPartitionsList().forEach(partition -> {
            partitions.put(partition.getPartitionId(), new PartitionImpl(partition));
            partitionIds.add(partition.getPartitionId());
        });
        Collections.sort(partitionIds);
    }

    @Override
    public PartitionGroupId id() {
        return group.getId();
    }

    @Override
    public Partition getPartition(int partitionId) {
        return partitions.get(partitionId);
    }

    @Override
    public List<Integer> getPartitionIds() {
        return partitionIds;
    }

    @Override
    public Collection<Partition> getPartitions() {
        return partitions.values();
    }
}
