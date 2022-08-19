// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.value;

import io.atomix.client.PrimitiveBuilder;
import io.grpc.Channel;

/**
 * Builder for AtomicValue.
 */
public abstract class AtomicValueBuilder<E> extends PrimitiveBuilder<AtomicValueBuilder<E>, AtomicValue<E>> {
    protected AtomicValueBuilder(String primitiveName, Channel channel) {
        super(primitiveName, channel);
    }
}
