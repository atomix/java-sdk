// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.iterator;

import java.util.Iterator;

/**
 * Synchronous iterator.
 */
public interface SyncIterator<T> extends Iterator<T> {

    /**
     * Closes the iterator.
     */
    void close();

    /**
     * Returns the underlying asynchronous iterator.
     *
     * @return the underlying asynchronous iterator
     */
    AsyncIterator<T> async();

}