// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.atomix.client.utils.channel.ChannelConfig;
import io.atomix.client.utils.channel.ChannelProvider;
import io.atomix.client.utils.channel.ServerChannelProvider;
import io.atomix.client.utils.channel.ServiceChannelProvider;
import io.atomix.client.utils.Builder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Atomix client builder.
 */
public class AtomixCloudBuilder implements Builder<AtomixCloud> {
    private static final String DEFAULT_NAMESPACE = "default";
    private String namespace = DEFAULT_NAMESPACE;
    private ChannelProvider brokerChannelProvider;
    private final ChannelConfig channelConfig = new ChannelConfig();

    /**
     * Sets the client namespace.
     *
     * @param namespace the client namespace
     * @return the client builder
     */
    public AtomixCloudBuilder withNamespace(String namespace) {
        this.namespace = checkNotNull(namespace);
        return this;
    }

    /**
     * Sets the broker server to which to connect.
     *
     * @param host the server host
     * @param port the server port
     * @return the client builder
     */
    public AtomixCloudBuilder withBrokerServer(String host, int port) {
        this.brokerChannelProvider = new ServerChannelProvider(host, port, channelConfig);
        return this;
    }

    /**
     * Sets the broker service to which to connect.
     *
     * @param serviceName the service name
     * @return the client builder
     */
    public AtomixCloudBuilder withServiceName(String serviceName) {
        this.brokerChannelProvider = new ServiceChannelProvider(serviceName, channelConfig);
        return this;
    }

    /**
     * Enables TLS.
     *
     * @return the client builder
     */
    public AtomixCloudBuilder withTlsEnabled() {
        return withTlsEnabled(true);
    }

    /**
     * Sets whether TLS is enabled.
     *
     * @param tlsEnabled whether to enable TLS
     * @return the client builder
     */
    public AtomixCloudBuilder withTlsEnabled(boolean tlsEnabled) {
        channelConfig.setTlsEnabled(tlsEnabled);
        return this;
    }

    /**
     * Sets the client key path.
     *
     * @param keyPath the client key path
     * @return the client builder
     */
    public AtomixCloudBuilder withKeyPath(String keyPath) {
        channelConfig.setKeyPath(keyPath);
        return this;
    }

    /**
     * Sets the client cert path.
     *
     * @param certPath the client cert path
     * @return the client builder
     */
    public AtomixCloudBuilder withCertPath(String certPath) {
        channelConfig.setCertPath(certPath);
        return this;
    }

    @Override
    public AtomixCloud build() {
        checkNotNull(brokerChannelProvider, "Broker channel provider cannot be null");
        return new AtomixCloud(namespace, brokerChannelProvider, channelConfig);
    }
}
