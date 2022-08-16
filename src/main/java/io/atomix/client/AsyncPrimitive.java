// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous primitive.
 */
public interface AsyncPrimitive extends DistributedPrimitive {

    /**
     * Closes the primitive.
     *
     * @return a future to be completed once the primitive is closed
     */
    CompletableFuture<Void> close();

    /**
     * Returns a synchronous wrapper around the asynchronous primitive.
     *
     * @return the synchronous primitive
     */
    SyncPrimitive sync();

    /**
     * Returns a synchronous wrapper around the asynchronous primitive.
     *
     * @param operationTimeout the synchronous operation timeout
     * @return the synchronous primitive
     */
    SyncPrimitive sync(Duration operationTimeout);

}
