// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.iterator;

/**
 * Synchronous iterable primitive.
 */
public interface SyncIterable<T> extends Iterable<T> {

    /**
     * Returns the synchronous iterator.
     *
     * @return the synchronous iterator
     */
    SyncIterator<T> iterator();

}