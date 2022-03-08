// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.channel;

/**
 * Channel provider.
 */
public interface ChannelProvider {

    /**
     * Returns a new channel factory for the given gRPC service.
     *
     * @return the channel factory
     */
    ChannelFactory getFactory();

}
