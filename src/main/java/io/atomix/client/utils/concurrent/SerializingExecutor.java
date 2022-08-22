package io.atomix.client.utils.concurrent;

import java.util.LinkedList;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

final class SerializingExecutor implements Executor {
    private final Executor parent;
    private final LinkedList<Runnable> tasks = new LinkedList<>();
    private boolean running;

    SerializingExecutor(Executor parent) {
        this.parent = parent;
    }

    private void run() {
        for (;;) {
            final Runnable task;
            synchronized (tasks) {
                task = tasks.poll();
                if (task == null) {
                    running = false;
                    return;
                }
            }
            task.run();
        }
    }

    @Override
    public void execute(Runnable command) {
        synchronized (tasks) {
            tasks.add(command);
            if (!running) {
                running = true;
                parent.execute(this::run);
            }
        }
    }
}
