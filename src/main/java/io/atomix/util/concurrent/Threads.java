package io.atomix.util.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;

import java.util.concurrent.ThreadFactory;

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
            .setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception on " + t.getName(), e))
            .build();
    }
}
