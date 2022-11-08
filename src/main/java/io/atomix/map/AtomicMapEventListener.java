// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.map;

import io.atomix.event.EventListener;

/**
 * Listener to be notified about updates to a ConsistentMap.
 */
public interface AtomicMapEventListener<K, V> extends EventListener<AtomicMapEvent<K, V>> {
}