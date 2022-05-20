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
     * Purges state associated with this primitive.
     * <p>
     * Implementations can override and provide appropriate clean up logic for purging
     * any state associated with the primitive. Whether modifications made within the
     * destroy method have local or global visibility is left unspecified.
     *
     * @return {@code CompletableFuture} that is completed when the operation completes
     */
    CompletableFuture<Void> destroy();

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
