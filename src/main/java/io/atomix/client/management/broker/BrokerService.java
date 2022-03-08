// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.management.broker;

import io.atomix.api.management.broker.PrimitiveAddress;
import io.atomix.api.primitive.PrimitiveId;

/**
 * Broker service. Provides methods to register and lookups primitives.
 */
public interface BrokerService {

    /**
     * Lookup primitive endpoint using the provided primitive id.
     *
     * @param primitiveId the id of the primitive to be looked up
     * @return the primitive endpoint
     */
    PrimitiveAddress lookupPrimitive(PrimitiveId primitiveId);

}
