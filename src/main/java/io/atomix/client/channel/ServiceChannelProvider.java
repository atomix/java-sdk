/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client.channel;

import io.atomix.client.AsyncAtomixClient;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
