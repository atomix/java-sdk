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
package io.atomix.client.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test protocol client.
 */
/*public class TestProtocolClient implements ProtocolClient {
  /*private final StateMachine stateMachine;
  private final Context context = new Context();

  TestProtocolClient(ServiceTypeRegistry serviceTypes) {
    this.stateMachine = new ServiceManagerStateMachine(serviceTypes);
    stateMachine.init(context);
  }

  @Override
  public CompletableFuture<byte[]> command(byte[] value) {
    return stateMachine.apply(new Command<>(context.getIndex(OperationType.COMMAND), System.currentTimeMillis(), value));
  }

  @Override
  public CompletableFuture<Void> command(byte[] value, StreamHandler<byte[]> handler) {
    return stateMachine.apply(new Command<>(context.getIndex(OperationType.COMMAND), System.currentTimeMillis(), value), handler);
  }

  @Override
  public CompletableFuture<byte[]> query(byte[] value) {
    return stateMachine.apply(new Query<>(context.getIndex(OperationType.QUERY), System.currentTimeMillis(), value));
  }

  @Override
  public CompletableFuture<Void> query(byte[] value, StreamHandler<byte[]> handler) {
    return stateMachine.apply(new Query<>(context.getIndex(OperationType.QUERY), System.currentTimeMillis(), value), handler);
  }

  private class Context implements StateMachine.Context {
    private final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    private final AtomicLong index = new AtomicLong();
    private OperationType operationType;

    long getIndex(OperationType type) {
      this.operationType = type;
      switch (type) {
        case COMMAND:
          return index.incrementAndGet();
        case QUERY:
          return index.get();
        default:
          throw new AssertionError();
      }
    }

    @Override
    public long getIndex() {
      return index.get();
    }

    @Override
    public long getTimestamp() {
      return System.currentTimeMillis();
    }

    @Override
    public OperationType getOperationType() {
      return operationType;
    }

    @Override
    public Logger getLogger() {
      return logger;
    }
  }
}*/
