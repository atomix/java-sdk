// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.map;

import io.atomix.client.PrimitiveBuilder;
import io.grpc.Channel;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Builder for AtomicCounter.
 */
public abstract class AtomicCounterMapBuilder<K> extends PrimitiveBuilder<AtomicCounterMapBuilder<K>, AtomicCounterMap<K>> {
    protected AtomicCounterMapBuilder(String primitiveName, Channel channel, ScheduledExecutorService executorService) {
        super(primitiveName, channel, executorService);
    }
}