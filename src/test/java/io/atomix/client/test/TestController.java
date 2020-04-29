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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import io.atomix.api.controller.ControllerServiceGrpc;
import io.atomix.api.controller.GetPartitionGroupsRequest;
import io.atomix.api.controller.GetPartitionGroupsResponse;
import io.atomix.api.controller.PartitionGroup;

import io.grpc.stub.StreamObserver;

/**
 * Test controller.
 */
/*public class TestController extends ControllerServiceGrpc.ControllerServiceImplBase implements Managed {
  /*private final ServiceRegistryImpl serviceRegistry;
  private final Collection<PartitionGroup> groups;

  public TestController(int port, Collection<PartitionGroup> groups) {
    this.serviceRegistry = new ServiceRegistryImpl(port);
    this.groups = groups;
  }

  @Override
  public void getPartitionGroups(GetPartitionGroupsRequest request, StreamObserver<GetPartitionGroupsResponse> responseObserver) {
    String name = !Strings.isNullOrEmpty(request.getId().getName()) ? request.getId().getName() : null;
    String namespace = !Strings.isNullOrEmpty(request.getId().getNamespace()) ? request.getId().getNamespace() : null;
    if (name != null && namespace != null) {
      responseObserver.onNext(GetPartitionGroupsResponse.newBuilder()
          .addAllGroups(groups.stream()
              .filter(group -> group.getId().getName().equals(name) && group.getId().getNamespace().equals(namespace))
              .collect(Collectors.toList()))
          .build());
    } else if (namespace != null) {
      responseObserver.onNext(GetPartitionGroupsResponse.newBuilder()
          .addAllGroups(groups.stream()
              .filter(group -> group.getId().getNamespace().equals(namespace))
              .collect(Collectors.toList()))
          .build());
    } else {
      responseObserver.onNext(GetPartitionGroupsResponse.newBuilder()
          .addAllGroups(groups)
          .build());
    }
    responseObserver.onCompleted();
  }

  @Override
  public CompletableFuture<Void> start() {
    return serviceRegistry.start()
        .thenRun(() -> serviceRegistry.register(this));
  }
}*/
