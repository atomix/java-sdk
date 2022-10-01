// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import java.time.Duration;

/**
 * Synchronous primitive.
 */
public interface SyncPrimitive<S extends SyncPrimitive<S, A>, A extends AsyncPrimitive<A, S>> extends DistributedPrimitive {
    Duration DEFAULT_OPERATION_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Closes the primitive.
     */
    void close();

    /**
     * Returns the underlying asynchronous primitive.
     *
     * @return the underlying asynchronous primitive
     */
    A async();

}
