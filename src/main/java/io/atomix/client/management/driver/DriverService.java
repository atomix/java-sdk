// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.management.driver;

import io.atomix.api.management.broker.PrimitiveAddress;
import io.grpc.Channel;

/**
 * Provides channels that connect to the primitives
 */
public interface DriverService {

    /**
     * Returns the driver channel bounded to the primitive address.
     *
     * @return the associated driver channel
     */
    Channel getDriverChannel(PrimitiveAddress primitiveAddress);

}
