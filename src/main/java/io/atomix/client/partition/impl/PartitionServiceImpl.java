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

import io.atomix.client.AsyncAtomixClient;
import io.atomix.client.partition.Partition;
import io.atomix.client.partition.PartitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Partition service implementation.
 */
public class PartitionServiceImpl implements PartitionService {
    private final Map<Integer, Partition> partitions = new ConcurrentHashMap<>();
    private final List<Integer> partitionIds = new CopyOnWriteArrayList<>();

    public PartitionServiceImpl(io.atomix.api.controller.Database database) {
        database.getPartitionsList().forEach(partition -> {
            partitions.put(partition.getPartitionId(), new PartitionImpl(partition));
            partitionIds.add(partition.getPartitionId());
        });
        Collections.sort(partitionIds);
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
