// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.atomix.client.utils.ThreadContext;
import io.grpc.Channel;

/**
 * Primitive type.
 */
public interface PrimitiveType<B extends PrimitiveBuilder, P extends SyncPrimitive> {

    /**
     * Returns a new primitive builder.
     *
     * @param primitiveName the primitive name
     * @param serviceChannel the channel to be used for the primitive services
     * @param threadContext the thread context
     * @return a new primitive builder
     */
    B newBuilder(String primitiveName, Channel serviceChannel, ThreadContext threadContext);

}
