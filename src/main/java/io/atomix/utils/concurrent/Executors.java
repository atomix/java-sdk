package io.atomix.utils.concurrent;

import java.util.concurrent.Executor;

public final class Executors {
    public static Executor newSerializingExecutor(Executor executor) {
        return new SerializingExecutor(executor);
    }
}
