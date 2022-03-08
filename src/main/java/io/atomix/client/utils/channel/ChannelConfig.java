// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.channel;

/**
 * TLS configuration.
 */
public class ChannelConfig {

    private boolean enabled = Boolean.parseBoolean(System.getProperty("io.atomix.enableNettyTLS", Boolean.toString(false)));
    private String certPath = System.getProperty("io.atomix.messaging.tls.certPath");
    private String keyPath = System.getProperty("io.atomix.messaging.tls.keyPath");

    /**
     * Returns whether TLS is enabled.
     *
     * @return indicates whether TLS is enabled
     */
    public boolean isTlsEnabled() {
        return enabled;
    }

    /**
     * Sets whether TLS is enabled.
     *
     * @param enabled whether TLS is enabled
     * @return the TLS configuration
     */
    public ChannelConfig setTlsEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Returns the certificate chain path.
     *
     * @return the certificate chain path
     */
    public String getCertPath() {
        return certPath;
    }

    /**
     * Sets the certificate chain path.
     *
     * @param certPath the certificate chain path
     * @return the TLS configuration
     */
    public ChannelConfig setCertPath(String certPath) {
        this.certPath = certPath;
        return this;
    }

    /**
     * Returns the private key path.
     *
     * @return the private key path
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * Sets the private key path.
     *
     * @param keyPath the private key path
     * @return the TLS configuration
     */
    public ChannelConfig setKeyPath(String keyPath) {
        this.keyPath = keyPath;
        return this;
    }
}
