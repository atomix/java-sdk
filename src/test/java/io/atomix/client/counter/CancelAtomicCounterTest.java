// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import atomix.runtime.counter.v1.CounterGrpc;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.client.AbstractPrimitiveTest;
import io.atomix.client.counter.impl.DefaultAsyncAtomicCounter;
import io.grpc.Context;
import io.grpc.Deadline;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static atomix.runtime.counter.v1.CounterOuterClass.*;
import static io.grpc.Status.UNAVAILABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AtomicCounter}
 *
public class CancelAtomicCounterTest extends AbstractPrimitiveTest {
    private static final String PRIMITIVE_NAME = "foo";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        serviceImpl = counterImplBase;
        super.setUp();
    }

    @Test
    public void testCancelledGet() throws ExecutionException, InterruptedException {
        AsyncAtomicCounter atomicCounter = new DefaultAsyncAtomicCounter(
                PRIMITIVE_NAME, channel, Context.current().withDeadlineAfter(
                1, TimeUnit.SECONDS, Executors.newSingleThreadScheduledExecutor()));
        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("context timed out");
        atomicCounter.get().get();
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
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    responseObserver.onNext(GetResponse.newBuilder()
                                                        .setOutput(getGetOutput(atomicLong.get()))
                                                        .build());
                    responseObserver.onCompleted();
                }
            }
    ));

}*/
