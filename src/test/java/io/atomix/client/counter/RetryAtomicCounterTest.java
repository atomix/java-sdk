// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import atomix.counter.v1.CounterGrpc;
import com.google.common.util.concurrent.AtomicDouble;
import io.atomix.client.AbstractPrimitiveTest;
import io.atomix.client.counter.impl.DefaultAsyncAtomicCounter;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static atomix.counter.v1.CounterOuterClass.*;
import static io.grpc.Status.UNAVAILABLE;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AtomicCounter}
 *
public class RetryAtomicCounterTest extends AbstractPrimitiveTest {
    private static final String PRIMITIVE_NAME = "foo";
    private static final Random random = new Random();
    private float unavailability;
    private final AtomicDouble retryCounter = new AtomicDouble(0.0);

    @Before
    public void setUp() throws Exception {
        retryCounter.set(0);
        serviceImpl = counterImplBase;
        super.setUp();
    }

    @Test
    public void testSuccessfulGet() {
        unavailability = 0.5F;
        AsyncAtomicCounter atomicCounter = new DefaultAsyncAtomicCounter(
                PRIMITIVE_NAME, channel, Context.current().withCancellation());
        assertEquals(Long.valueOf(1), futureGetOrElse(atomicCounter.get(), 0L));
        assertTrue(retryCounter.get() >= 0 && retryCounter.get() < maxAttempts);
    }

    @Test
    public void testUnavailableGet() {
        unavailability = 1.0F;
        AsyncAtomicCounter atomicCounter = new DefaultAsyncAtomicCounter(
                PRIMITIVE_NAME, channel, Context.current().withCancellation());
        assertEquals(Long.valueOf(0), futureGetOrElse(atomicCounter.get(), 0L));
        assertEquals(retryCounter.get(), maxAttempts, 0.0);
    }

    private final AtomicLong atomicLong = new AtomicLong(1);

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
                    retryCounter.addAndGet(1);
                    if (random.nextFloat() < unavailability) {
                        responseObserver.onError(UNAVAILABLE.withDescription("Counter temporarily unavailable...")
                                                         .asRuntimeException());
                    } else {
                        responseObserver.onNext(GetResponse.newBuilder()
                                                        .setOutput(getGetOutput(atomicLong.get()))
                                                        .build());
                        responseObserver.onCompleted();
                    }
                }
            }
    ));

}*/
