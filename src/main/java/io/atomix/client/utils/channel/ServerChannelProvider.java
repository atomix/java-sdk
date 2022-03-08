// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.channel;

import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/**
 * Server channel provider.
 */
public class ServerChannelProvider implements ChannelProvider {

    private final String host;
    private final int port;
    private final ChannelConfig config;

    public ServerChannelProvider(String host, int port, ChannelConfig config) {
        this.host = host;
        this.port = port;
        this.config = config;
    }

    @Override
    public ChannelFactory getFactory() {
        NettyChannelBuilder builder;
        if (config.isTlsEnabled()) {
            builder = NettyChannelBuilder.forAddress(host, port)
                .useTransportSecurity();
        } else {
            builder = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext();
        }
        return builder::build;
    }

}
