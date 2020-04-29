/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Primary interface for managing Atomix clusters and operating on distributed primitives.
 */
public class AtomixClient {

    /**
     * Returns a new Atomix client builder.
     *
     * @return a new Atomix builder
     */
    public static AtomixClientBuilder builder() {
        return new AtomixClientBuilder();
    }
    private static final long TIMEOUT_MILLIS = Duration.ofSeconds(30).toMillis();

    private final AsyncAtomixClient asyncClient;

    protected AtomixClient(AsyncAtomixClient asyncClient) {
        this.asyncClient = checkNotNull(asyncClient);
    }

    /**
     * Returns the asynchronous client.
     *
     * @return the asynchronous client
     */
    public AsyncAtomixClient async() {
        return asyncClient;
    }

    /**
     * Returns a list of databases in the cluster.
     *
     * @return a list of databases supported by the controller
     */
    public Collection<AtomixDatabase> getDatabases() {
        return complete(asyncClient.getDatabases());
    }

    /**
     * Returns a database by name.
     *
     * @param name the database name
     * @return the database
     */
    public AtomixDatabase getDatabase(String name) {
        return complete(asyncClient.getDatabase(name));
    }

    protected <T> T complete(CompletableFuture<T> future) {
        try {
            return future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrimitiveException.Interrupted();
        } catch (TimeoutException e) {
            throw new PrimitiveException.ConcurrentModification();
        } catch (ExecutionException e) {
            Throwable cause = Throwables.getRootCause(e);
            if (cause instanceof PrimitiveException) {
                throw (PrimitiveException) cause;
            } else {
                throw new PrimitiveException(cause);
            }
        }
    }

    /**
     * Connects the client.
     */
    public void connect() {
        complete(asyncClient.connect());
    }

    /**
     * Returns a boolean indicating whether the instance is running.
     *
     * @return indicates whether the instance is running
     */
    public boolean isRunning() {
        return asyncClient.isRunning();
    }

    /**
     * Closes the client.
     */
    public synchronized void close() {
        complete(asyncClient.close());
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }
}
