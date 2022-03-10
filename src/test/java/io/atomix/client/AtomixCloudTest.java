// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for AtomixCloud client
 */
public class AtomixCloudTest {

    private AtomixCloud atomixCloud;
    private AtomixCloud atomixCloudWithTls;

    @Before
    public void setUp() throws Exception {
        atomixCloud = AtomixCloud.builder()
                .withBrokerServer("pippo", 12345)
                .withNamespace("tost")
                .build();
        atomixCloudWithTls = AtomixCloud.builder()
                .withBrokerServer("pippo", 12345)
                .withNamespace("tost")
                .withCertPath("/foo")
                .withKeyPath("/bar")
                .withTlsEnabled()
                .build();
    }

    @Test
    public void testStart() {
        assertFalse(atomixCloud.isRunning());
        atomixCloud.start();
        assertTrue(atomixCloud.isRunning());

        assertFalse(atomixCloudWithTls.isRunning());
        atomixCloudWithTls.start();
        assertTrue(atomixCloudWithTls.isRunning());
    }

    @Test
    public void testStop() {
        testStart();

        assertTrue(atomixCloud.isRunning());
        atomixCloud.stop();
        assertFalse(atomixCloud.isRunning());

        assertTrue(atomixCloudWithTls.isRunning());
        atomixCloudWithTls.stop();
        assertFalse(atomixCloudWithTls.isRunning());
    }

}
