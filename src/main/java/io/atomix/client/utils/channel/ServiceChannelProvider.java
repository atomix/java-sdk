// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.channel;

import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/**
 * Service-based channel factory.
 */
public class ServiceChannelProvider implements ChannelProvider {
    private final String service;
    private final ChannelConfig config;

    public ServiceChannelProvider(String service, ChannelConfig config) {
        this.service = service;
        this.config = config;
    }

    @Override
    public ChannelFactory getFactory() {
        NettyChannelBuilder builder;
        if (config.isTlsEnabled()) {
            builder = NettyChannelBuilder.forTarget(service)
                .nameResolverFactory(new DnsNameResolverProvider())
                .useTransportSecurity();
        } else {
            builder = NettyChannelBuilder.forTarget(service)
                .nameResolverFactory(new DnsNameResolverProvider())
                .usePlaintext();
        }
        return builder::build;
    }
}
