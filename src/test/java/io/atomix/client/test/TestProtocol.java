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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.Empty;


/**
 * Test protocol.
 */
/*public class TestProtocol implements ServiceProtocol {
  /*public static final Type TYPE = new Type();

  private static final Map<PartitionId, TestProtocol> partitions = new ConcurrentHashMap<>();

  public static void reset() {
    partitions.clear();
  }

  @Component
  public static class Type implements Protocol.Type<Empty> {
    private static final String NAME = "test";

    @Override
    public String name() {
      return NAME;
    }

    @Override
    public Empty parseConfig(InputStream is) throws IOException {
      throw new UnsupportedEncodingException();
    }

    @Override
    public Protocol newProtocol(Empty config, ProtocolManagementService managementService) {
      return partitions.computeIfAbsent(managementService.getPartitionService().getPartitionId(), id -> new TestProtocol(managementService));
    }
  }

  private final ProtocolManagementService managementService;

  public TestProtocol(ProtocolManagementService managementService) {
    this.managementService = managementService;
  }

  @Override
  public ProtocolClient getServiceClient() {
    return new TestProtocolClient(managementService.getServiceTypeRegistry());
  }
}*/
