// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.concurrent;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Blocking aware thread pool context.
 */
public class BlockingAwareThreadPoolContext extends ThreadPoolContext {
    public BlockingAwareThreadPoolContext(ScheduledExecutorService parent) {
        super(parent);
    }

    @Override
    public void execute(Runnable command) {
        if (isBlocked()) {
            parent.execute(command);
        } else {
            super.execute(command);
        }
    }
}
