// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map;

import io.atomix.AtomixChannel;
import io.atomix.PrimitiveBuilder;

/**
 * Builder for AtomicCounter.
 */
public abstract class DistributedMultimapBuilder<K, V> extends PrimitiveBuilder<DistributedMultimapBuilder<K, V>, DistributedMultimap<K, V>> {
    protected DistributedMultimapBuilder(AtomixChannel channel) {
        super(channel);
    }
}
