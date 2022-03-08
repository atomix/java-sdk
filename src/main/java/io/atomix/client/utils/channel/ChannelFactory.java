// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.channel;

import io.grpc.Channel;

/**
 * Channel factory.
 */
@FunctionalInterface
public interface ChannelFactory {

    /**
     * Returns a new gRPC channel for the client.
     *
     * @return the gRPC channel
     */
    Channel getChannel();

}
