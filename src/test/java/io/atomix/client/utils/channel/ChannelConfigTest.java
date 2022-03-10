// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.channel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Channel config test.
 */
public class ChannelConfigTest {
    private static final String CERT_PATH = "/foo/bar";
    private static final String KEY_PATH = "/foo/bar";

    /**
     * Tests channel config builder.
     */
    @Test
    public void testChannelConfigBuilder() {
        ChannelConfig channelConfig = new ChannelConfig();
        assertNull(channelConfig.getCertPath());
        assertNull(channelConfig.getKeyPath());
        assertFalse(channelConfig.isTlsEnabled());

        channelConfig.setCertPath(CERT_PATH);
        channelConfig.setKeyPath(KEY_PATH);
        channelConfig.setTlsEnabled(true);
        assertEquals(CERT_PATH, channelConfig.getCertPath());
        assertEquals(KEY_PATH, channelConfig.getKeyPath());
        assertTrue(channelConfig.isTlsEnabled());
    }

}
