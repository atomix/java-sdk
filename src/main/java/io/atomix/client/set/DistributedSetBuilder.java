// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.set;

import io.atomix.client.AtomixChannel;
import io.atomix.client.PrimitiveBuilder;

/**
 * Builder for DistributedSet.
 */
public abstract class DistributedSetBuilder<E> extends PrimitiveBuilder<DistributedSetBuilder<E>, DistributedSet<E>> {
    protected DistributedSetBuilder(AtomixChannel channel) {
        super(channel);
    }
}
