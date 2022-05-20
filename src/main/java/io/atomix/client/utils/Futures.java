// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Utilities for creating completed and exceptional futures.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Futures {

    /**
     * Returns the future value when complete or if future
     * completes exceptionally returns the defaultValue.
     *
     * @param future future
     * @param defaultValue default value
     * @param <T> future value type
     * @return future value when complete or if future
     * completes exceptionally returns the defaultValue.
     */
    public static <T> T futureGetOrElse(Future<T> future, T defaultValue) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return defaultValue;
        } catch (ExecutionException e) {
            return defaultValue;
        }
    }

}
