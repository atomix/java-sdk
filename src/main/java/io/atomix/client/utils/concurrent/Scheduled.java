// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.concurrent;

/**
 * Scheduled task.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Scheduled {

    /**
     * Cancels the scheduled task.
     */
    void cancel();

}
