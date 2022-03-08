// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.management.broker.impl;

import io.atomix.api.management.broker.BrokerGrpc;
import io.atomix.api.management.broker.LookupPrimitiveRequest;
import io.atomix.api.management.broker.LookupPrimitiveResponse;
import io.atomix.api.management.broker.PrimitiveAddress;
import io.atomix.api.management.broker.PrimitiveId;
import io.atomix.client.management.broker.BrokerService;
import io.atomix.client.utils.channel.ChannelFactory;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;

import static io.atomix.client.utils.concurrent.Futures.futureGetOrElse;

/**
 * Default implementation of the broker service APIs.
 */
public class DefaultBroker implements BrokerService {

    private final BrokerGrpc.BrokerStub service;

    /**
     * Builds a new broker using the given channel factory.
     *
     * @param channelFactory channel factory object
     */
    public DefaultBroker(ChannelFactory channelFactory) {
        this.service = BrokerGrpc.newStub(channelFactory.getChannel());
    }

    /**
     * Builds a new broker using the supplied channel. Mostly
     * used for testing.
     *
     * @param channel the channel connected to the service
     */
    public DefaultBroker(Channel channel) {
        this.service = BrokerGrpc.newStub(channel);
    }

    @Override
    public PrimitiveAddress lookupPrimitive(io.atomix.api.primitive.PrimitiveId primitiveId) {
        final CompletableFuture<PrimitiveAddress> future = new CompletableFuture<>();
        final StreamObserver<LookupPrimitiveResponse> streamObserver = new StreamObserver<>() {
            @Override
            public void onNext(LookupPrimitiveResponse lookupPrimitiveResponse) {
                future.complete(lookupPrimitiveResponse.getAddress());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {

            }
        };
        service.lookupPrimitive(LookupPrimitiveRequest.newBuilder()
                .setPrimitiveId(PrimitiveId.newBuilder()
                        .setId(primitiveId)
                        .build())
                .build(), streamObserver);
        return futureGetOrElse(future, null);
    }

}
