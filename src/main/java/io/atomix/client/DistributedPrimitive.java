// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

/**
 * Interface for all distributed primitives.
 */
public interface DistributedPrimitive {

    /**
     * Default timeout for primitive operations.
     */
    long DEFAULT_OPERATION_TIMEOUT_MILLIS = 5000L;

    /**
     * Returns the name of this primitive.
     *
     * @return name
     */
    String name();
}
