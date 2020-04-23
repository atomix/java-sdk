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
package io.atomix.client.partition;

import java.util.Collection;
import java.util.List;

/**
 * Partition service.
 */
public interface PartitionService {

    /**
     * Returns a partition by ID.
     *
     * @param partitionId the partition ID
     * @return the partition
     */
    Partition getPartition(int partitionId);

    /**
     * Returns the partitions in the group.
     *
     * @return the partitions in the group
     */
    Collection<Partition> getPartitions();

    /**
     * Returns a list of partition IDs in the partition group.
     *
     * @return a list of partition IDs
     */
    List<Integer> getPartitionIds();

}
