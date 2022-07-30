// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import io.atomix.client.PrimitiveBuilder;
import io.atomix.client.PrimitiveManagementService;
import io.grpc.Channel;
import io.grpc.Context;

/**
 * Builder for AtomicCounter.
 */
public abstract class AtomicCounterBuilder extends PrimitiveBuilder<AtomicCounterBuilder, AtomicCounter> {

    protected AtomicCounterBuilder(String primitiveName, Channel channel, PrimitiveManagementService primitiveManagementService) {
        super(primitiveName, channel, primitiveManagementService);
    }
}
