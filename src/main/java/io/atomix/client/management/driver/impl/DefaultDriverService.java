// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.management.driver.impl;

import com.google.common.collect.Maps;
import io.atomix.api.management.broker.PrimitiveAddress;
import io.atomix.client.management.driver.DriverService;
import io.atomix.client.utils.channel.ChannelConfig;
import io.atomix.client.utils.channel.ChannelFactory;
import io.atomix.client.utils.channel.ServerChannelProvider;
import io.grpc.Channel;

import java.util.Map;

/**
 * Default implementation of the driver service APIs.
 */
public class DefaultDriverService implements DriverService {

    private final Map<PrimitiveAddress, ChannelFactory> drivers = Maps.newConcurrentMap();
    private final ChannelConfig channelConfig;

    /**
     * Builds a new driver service using the given channel provider.
     *
     * @param cConfig the channel config.
     */
    public DefaultDriverService(ChannelConfig cConfig) {
        channelConfig = cConfig;
    }

    @Override
    public Channel getDriverChannel(PrimitiveAddress primitiveAddress) {
        return drivers.computeIfAbsent(primitiveAddress, driver -> new ServerChannelProvider(
                primitiveAddress.getHost(), primitiveAddress.getPort(), channelConfig).getFactory()).getChannel();
    }

}
