// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import io.atomix.client.PrimitiveBuilder;
import io.grpc.Channel;

/**
 * Builder for AtomicCounter.
 */
public abstract class AtomicCounterBuilder extends PrimitiveBuilder<AtomicCounterBuilder, AtomicCounter> {

    protected AtomicCounterBuilder(String primitiveName, Channel channel) {
        super(primitiveName, channel);
    }
}
