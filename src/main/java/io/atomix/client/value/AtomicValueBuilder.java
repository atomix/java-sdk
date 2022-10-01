// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.value;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;

/**
 * Builder for AtomicValue.
 */
public abstract class AtomicValueBuilder<E> extends PrimitiveBuilder<AtomicValueBuilder<E>, AtomicValue<E>> {
    protected AtomicValueBuilder(AtomixChannel channel) {
        super(channel);
    }
}
