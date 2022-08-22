// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set;

import io.atomix.client.PrimitiveBuilder;
import io.grpc.Channel;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Builder for DistributedSet.
 */
public abstract class DistributedSetBuilder<E> extends PrimitiveBuilder<DistributedSetBuilder<E>, DistributedSet<E>> {
    protected DistributedSetBuilder(String primitiveName, Channel channel, ScheduledExecutorService executorService) {
        super(primitiveName, channel, executorService);
    }
}
