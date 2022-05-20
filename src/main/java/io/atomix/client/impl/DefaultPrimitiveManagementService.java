// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.impl;

import com.google.common.collect.Maps;
import io.atomix.client.DistributedPrimitive;
import io.atomix.client.PrimitiveManagementService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DefaultPrimitiveManagementService implements PrimitiveManagementService {
    private final Map<String, CompletableFuture> primitives = Maps.newConcurrentMap();

    @Override
    @SuppressWarnings("unchecked")
    public <P extends DistributedPrimitive> CompletableFuture<P> getPrimitive(String name, Supplier<CompletableFuture<P>> supplier) {
        return primitives.computeIfAbsent(name, n -> supplier.get());
    }
}
