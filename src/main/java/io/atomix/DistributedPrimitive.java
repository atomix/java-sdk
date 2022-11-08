// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix;

/**
 * Interface for all distributed primitives.
 */
public interface DistributedPrimitive {
    /**
     * Returns the name of this primitive.
     *
     * @return name
     */
    String name();
}
