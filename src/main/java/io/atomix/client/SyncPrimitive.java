// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

/**
 * Synchronous primitive.
 */
public interface SyncPrimitive extends DistributedPrimitive {

    /**
     * Closes the primitive.
     */
    void close();

    /**
     * Returns the underlying asynchronous primitive.
     *
     * @return the underlying asynchronous primitive
     */
    AsyncPrimitive async();

}
