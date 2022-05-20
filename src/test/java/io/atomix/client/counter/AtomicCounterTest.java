// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import atomix.counter.v1.CounterGrpc;
import io.atomix.client.AbstractPrimitiveTest;
import io.atomix.client.counter.impl.DefaultAsyncAtomicCounter;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static atomix.counter.v1.CounterOuterClass.*;
import static io.atomix.client.utils.Futures.futureGetOrElse;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AtomicCounter}
 */
public class AtomicCounterTest extends AbstractPrimitiveTest {

    private static final String PRIMITIVE_NAME = "foo";

    @Before
    public void setUp() throws Exception {
        serviceImpl = counterImplBase;
        super.setUp();
    }

    @Test
    public void testBasicOperations() {
        AsyncAtomicCounter atomicCounter = new DefaultAsyncAtomicCounter(PRIMITIVE_NAME, channel, null);
        assertEquals(Long.valueOf(0), futureGetOrElse(atomicCounter.get(), 0L));

        atomicCounter.set(100);
        assertEquals(Long.valueOf(100), futureGetOrElse(atomicCounter.get(), 0L));

        assertEquals(Long.valueOf(89), futureGetOrElse(atomicCounter.addAndGet(-11L), 0L));

        assertFalse(futureGetOrElse(atomicCounter.compareAndSet(100, 101), false));
        assertEquals(Long.valueOf(89), futureGetOrElse(atomicCounter.get(), 0L));
        assertTrue(futureGetOrElse(atomicCounter.compareAndSet(89, 101), false));
        assertEquals(Long.valueOf(101), futureGetOrElse(atomicCounter.get(), 0L));

        assertEquals(Long.valueOf(101), futureGetOrElse(atomicCounter.getAndAdd(1), 0L));
        assertEquals(Long.valueOf(102), futureGetOrElse(atomicCounter.get(), 0L));

        assertEquals(Long.valueOf(103), futureGetOrElse(atomicCounter.incrementAndGet(), 0L));

        assertEquals(Long.valueOf(103), futureGetOrElse(atomicCounter.getAndIncrement(), 0L));
        assertEquals(Long.valueOf(104), futureGetOrElse(atomicCounter.get(), 0L));

        assertEquals(Long.valueOf(103), futureGetOrElse(atomicCounter.decrementAndGet(), 0L));

        assertEquals(Long.valueOf(103), futureGetOrElse(atomicCounter.getAndDecrement(), 0L));
        assertEquals(Long.valueOf(102), futureGetOrElse(atomicCounter.get(), 0L));
    }

    private final AtomicLong atomicLong = new AtomicLong(0);

    // Mock implementation of the counter server
    private final CounterGrpc.CounterImplBase counterImplBase = mock(CounterGrpc.CounterImplBase.class, delegatesTo(
            new CounterGrpc.CounterImplBase() {

                private GetOutput getGetOutput(long value) {
                    return GetOutput.newBuilder()
                            .setValue(value)
                            .build();
                }

                private SetOutput getSetOutput(long value) {
                    return SetOutput.newBuilder()
                            .setValue(value)
                            .build();
                }

                private IncrementOutput getIncrementOutput(long value) {
                    return IncrementOutput.newBuilder()
                            .setValue(value)
                            .build();
                }

                private DecrementOutput getDecrementOutput(long value) {
                    return DecrementOutput.newBuilder()
                            .setValue(value)
                            .build();
                }

                @Override
                public void set(SetRequest request, StreamObserver<SetResponse> responseObserver) {
                    List<Precondition> preconditionList = request.getInput().getPreconditionsList();
                    long updatedValue = request.getInput().getValue();
                    if (preconditionList.isEmpty()) {
                        atomicLong.set(updatedValue);
                    } else {
                        atomicLong.compareAndSet(preconditionList.get(0).getValue(), updatedValue);
                    }
                    responseObserver.onNext(SetResponse.newBuilder()
                                                    .setOutput(getSetOutput(atomicLong.get()))
                                                    .build());
                    responseObserver.onCompleted();
                }

                @Override
                public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
                    responseObserver.onNext(GetResponse.newBuilder()
                                                    .setOutput(getGetOutput(atomicLong.get()))
                                                    .build());
                    responseObserver.onCompleted();
                }

                @Override
                public void increment(IncrementRequest request, StreamObserver<IncrementResponse> responseObserver) {
                    long value = atomicLong.addAndGet(request.getInput().getDelta());
                    responseObserver.onNext(IncrementResponse.newBuilder()
                                                    .setOutput(getIncrementOutput(value))
                                                    .build());
                    responseObserver.onCompleted();
                }

                @Override
                public void decrement(DecrementRequest request, StreamObserver<DecrementResponse> responseObserver) {
                    long value = atomicLong.decrementAndGet();
                    responseObserver.onNext(DecrementResponse.newBuilder()
                                                    .setOutput(getDecrementOutput(value))
                                                    .build());
                    responseObserver.onCompleted();
                }
            }
    ));

}
