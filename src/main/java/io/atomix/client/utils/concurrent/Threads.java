// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.concurrent;

import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;

/**
 * Thread utilities.
 */
public final class Threads {

    /**
     * Returns a thread factory that produces threads named according to the
     * supplied name pattern.
     *
     * @param pattern name pattern
     * @return thread factory
     */
    public static ThreadFactory namedThreads(String pattern, Logger log) {
        return new ThreadFactoryBuilder()
            .setNameFormat(pattern)
            .setThreadFactory(new AtomixThreadFactory())
            .setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception on " + t.getName(), e))
            .build();
    }
}
