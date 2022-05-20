// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

/**
 * Synchronous primitive.
 */
public interface SyncPrimitive extends DistributedPrimitive {

    /**
     * Purges state associated with this primitive.
     * <p>
     * Implementations can override and provide appropriate clean up logic for purging
     * any state associated with the primitive. Whether modifications made within the
     * destroy method have local or global visibility is left unspecified.
     */
    default void destroy() {
    }

    /**
     * Closes the primitive.
     */
    void close();

    /**
     * Returns the underlying asynchronous primitive.
     *
     * @return the underlying asynchronous primitive
     */
    AsyncPrimitive async();

}
