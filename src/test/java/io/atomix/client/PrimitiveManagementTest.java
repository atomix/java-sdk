// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;


import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.counter.impl.BlockingAtomicCounter;
import io.atomix.client.counter.impl.DefaultAsyncAtomicCounter;
import io.atomix.client.impl.DefaultPrimitiveManagementService;

import io.grpc.Channel;
import io.grpc.Context;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;
import static org.mockito.Mockito.mock;

/**
 * Tests for primitive management service.
 */
public class PrimitiveManagementTest {

    private PrimitiveManagementService primitiveManagementService;

    @Before
    public void setUp() {
        primitiveManagementService = new DefaultPrimitiveManagementService();
    }

    @Test
    public void testGetPrimitive() {
        CompletableFuture<AtomicCounter> counter = primitiveManagementService.getPrimitive(
                "counter", this::counterSupplier);
        CompletableFuture<AtomicCounter> sameCounter = primitiveManagementService.getPrimitive(
                "counter", this::counterSupplier);
        assertEquals(counter, sameCounter);

        sameCounter = primitiveManagementService.getPrimitive(
                "counter", this::differentCounterSupplier);
        assertEquals(counter, sameCounter);

        CompletableFuture<AtomicCounter> differentCounter = primitiveManagementService.getPrimitive(
                "differentCounter", this::differentCounterSupplier);
        assertNotSame(counter, differentCounter);
    }

    private CompletableFuture<AtomicCounter> counterSupplier() {
        return CompletableFuture.completedFuture(new BlockingAtomicCounter(new DefaultAsyncAtomicCounter(
                "counter",
                "onos",
                "0",
                mock(Channel.class),
                mock(Context.class)), DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS));
    }

    private CompletableFuture<AtomicCounter> differentCounterSupplier() {
        return CompletableFuture.completedFuture(new BlockingAtomicCounter(new DefaultAsyncAtomicCounter(
                "differentCounter",
                "onos",
                "0",
                mock(Channel.class),
                mock(Context.class)), DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS));
    }
}
