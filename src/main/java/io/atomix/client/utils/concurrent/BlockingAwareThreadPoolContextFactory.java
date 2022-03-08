// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;

import static io.atomix.client.utils.concurrent.Threads.namedThreads;

/**
 * Thread pool context factory.
 */
public class BlockingAwareThreadPoolContextFactory implements ThreadContextFactory {
    private final ScheduledExecutorService executor;

    public BlockingAwareThreadPoolContextFactory(String name, int threadPoolSize, Logger logger) {
        this(threadPoolSize, namedThreads(name, logger));
    }

    public BlockingAwareThreadPoolContextFactory(int threadPoolSize, ThreadFactory threadFactory) {
        this(Executors.newScheduledThreadPool(threadPoolSize, threadFactory));
    }

    public BlockingAwareThreadPoolContextFactory(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public ThreadContext createContext() {
        return new BlockingAwareThreadPoolContext(executor);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
