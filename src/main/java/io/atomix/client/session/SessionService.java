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
package io.atomix.client.session;

import io.atomix.client.AsyncAtomixClient;
import io.atomix.client.partition.PartitionService;
import io.atomix.client.utils.concurrent.Futures;
import io.atomix.client.utils.concurrent.ThreadContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages sessions for a single database.
 */
public class SessionService {
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    public SessionService(PartitionService partitions, ThreadContextFactory threadContextFactory, Duration timeout) {
        partitions.getPartitions().forEach(partition -> {
            sessions.put(partition.id(), new Session(partition, threadContextFactory.createContext(), timeout));
        });
    }

    /**
     * Returns a session by partition ID.
     *
     * @param partitionId the partition identifier
     * @return the session for the given partition
     */
    public Session getSession(int partitionId) {
        return sessions.get(partitionId);
    }

    /**
     * Returns the collection of sessions.
     *
     * @return the collection of sessions
     */
    public Collection<Session> getSessions() {
        return sessions.values();
    }

    /**
     * Connects all sessions.
     *
     * @return a future to be completed once the sessions are connected
     */
    public CompletableFuture<Void> connect() {
        return Futures.allOf(sessions.values().stream().map(Session::connect)).thenApply(v -> null);
    }

    /**
     * Closes all sessions.
     *
     * @return a future to be completed once the sessions are closed
     */
    public CompletableFuture<Void> close() {
        return Futures.allOf(sessions.values().stream().map(Session::close)).thenApply(v -> null);
    }
}
