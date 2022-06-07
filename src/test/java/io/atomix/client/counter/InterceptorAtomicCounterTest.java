// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import atomix.counter.v1.CounterGrpc;
import io.atomix.client.AbstractPrimitiveTest;
import io.atomix.client.Constants;
import io.atomix.client.counter.impl.DefaultAsyncAtomicCounter;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static atomix.counter.v1.CounterOuterClass.*;
import static io.atomix.client.utils.Futures.futureGetOrElse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AtomicCounter}
 */
public class InterceptorAtomicCounterTest extends AbstractPrimitiveTest {
    private static final String PRIMITIVE_NAME_1 = "foo";
    private static final String PRIMITIVE_NAME_2 = "pippo";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        serviceImpl = counterImplBase;
        serverInterceptor = new PrimitiveServerInterceptor();
        super.setUp();
    }

    @Test
    public void testMetaGet() throws ExecutionException, InterruptedException {
        Context current = Context.current().withCancellation();
        AsyncAtomicCounter atomicCounter1 = new DefaultAsyncAtomicCounter(
                PRIMITIVE_NAME_1, channel, current);
        AsyncAtomicCounter atomicCounter2 = new DefaultAsyncAtomicCounter(
                PRIMITIVE_NAME_2, channel, current);
        CompletableFuture<Long> get2 = atomicCounter2.get();
        CompletableFuture<Long> get1 = atomicCounter1.get();
        assertEquals(Long.valueOf(1), futureGetOrElse(get1, 0L));
        assertEquals(Long.valueOf(1), futureGetOrElse(get2, 0L));
        assertNotNull(verifyPrimitiveName1);
        assertNotNull(verifyPrimitiveName2);
        assertEquals(PRIMITIVE_NAME_1, verifyPrimitiveName1);
        assertEquals(PRIMITIVE_NAME_2, verifyPrimitiveName2);
    }

    private final AtomicLong atomicLong = new AtomicLong(1);
    private String verifyPrimitiveName1;
    private String verifyPrimitiveName2;

    // Mock implementation of the counter server
    private final CounterGrpc.CounterImplBase counterImplBase = mock(CounterGrpc.CounterImplBase.class, delegatesTo(
            new CounterGrpc.CounterImplBase() {

                private GetOutput getGetOutput(long value) {
                    return GetOutput.newBuilder()
                            .setValue(value)
                            .build();
                }

                @Override
                public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
                    responseObserver.onNext(GetResponse.newBuilder()
                                                        .setOutput(getGetOutput(atomicLong.get()))
                                                        .build());
                    responseObserver.onCompleted();
                }
            }
    ));

    private final class PrimitiveServerInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                     Metadata metadata,
                                                                     ServerCallHandler<ReqT, RespT> serverCallHandler) {
            String primitiveId = metadata.get(Constants.PRIMITIVE_ID_META);
            if (Objects.equals(primitiveId, PRIMITIVE_NAME_1)) {
                verifyPrimitiveName1 = primitiveId;
            } else if (Objects.equals(primitiveId, PRIMITIVE_NAME_2)){
                verifyPrimitiveName2 = primitiveId;
            }
            return Contexts.interceptCall(Context.current(), serverCall, metadata, serverCallHandler);
        }
    }

}
