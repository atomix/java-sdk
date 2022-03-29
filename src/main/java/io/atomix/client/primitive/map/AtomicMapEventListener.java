// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.primitive.map;

import io.atomix.client.primitive.event.EventListener;

/**
 * Listener to be notified about updates to a ConsistentMap.
 */
public interface AtomicMapEventListener<K, V> extends EventListener<AtomicMapEvent<K, V>> {
}
