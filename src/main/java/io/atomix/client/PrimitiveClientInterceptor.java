// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class PrimitiveClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions,
                                                               Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String appId = Constants.APPLICATION_ID_CTX.get();
                String primitiveId = Constants.PRIMITIVE_ID_CTX.get();
                String sessionId = Constants.SESSION_ID_CTX.get();
                if (appId != null && primitiveId != null && sessionId != null) {
                    headers.put(Constants.APPLICATION_ID_META, appId);
                    headers.put(Constants.PRIMITIVE_ID_META, primitiveId);
                    headers.put(Constants.SESSION_ID_META, sessionId);
                }
                super.start(responseListener, headers);
            }
        };
    }
}
