// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.grpc.Channel;
import io.grpc.Context;

/**
 * Primitive type.
 */
public interface PrimitiveType<B extends PrimitiveBuilder, P extends SyncPrimitive> {

    /**
     * Returns a new primitive builder.
     *
     * @param primitiveName the primitive name
     * @param serviceChannel the channel to be used for the primitive services
     * @param context the context
     * @return a new primitive builder
     */
    B newBuilder(String primitiveName, Channel serviceChannel, Context context);

}
