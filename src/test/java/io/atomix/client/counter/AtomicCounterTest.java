// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.counter;

import io.atomix.api.runtime.counter.v1.*;
import io.atomix.client.AbstractPrimitiveTest;
import io.atomix.client.PrimitiveException;
import io.atomix.client.counter.impl.DefaultAsyncAtomicCounter;
import io.atomix.client.counter.impl.DefaultAtomicCounterBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AtomicCounter}
 */
public class AtomicCounterTest extends AbstractPrimitiveTest {

    private static final String PRIMITIVE_NAME = "counter";
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        serviceImpl = counterImplBase;
        super.setUp();
    }

    @Test
    public void testNotFound() throws ExecutionException, InterruptedException {
        AsyncAtomicCounter atomicCounter = new DefaultAsyncAtomicCounter(PRIMITIVE_NAME, CounterGrpc.newStub(channel), EXECUTOR);
        exceptionRule.expect(ExecutionException.class);
        exceptionRule.expectMessage("counter not found");
        assertEquals(Long.valueOf(0), atomicCounter.get().get());
    }

    @Test
    public void testBasicOperations() {
        AtomicCounter atomicCounter = buildAtomicCounter();
        assertEquals(0, atomicCounter.get());
        atomicCounter.set(100);
        assertEquals(100, atomicCounter.get());
        assertEquals(89, atomicCounter.addAndGet(-11L));
        assertFalse(atomicCounter.compareAndSet(100, 101));
        assertEquals(89, atomicCounter.get());
        assertTrue(atomicCounter.compareAndSet(89, 101));
        assertEquals(101, atomicCounter.get());
        assertEquals(101, atomicCounter.getAndAdd(1));
        assertEquals(102, atomicCounter.get());
        assertEquals(103, atomicCounter.incrementAndGet());
        assertEquals(103, atomicCounter.getAndIncrement());
        assertEquals(104, atomicCounter.get());
        assertEquals(103, atomicCounter.decrementAndGet());
        assertEquals(103, atomicCounter.getAndDecrement());
        assertEquals(102, atomicCounter.get());
    }

    @Test
    public void testClose() {
        AtomicCounter atomicCounter = buildAtomicCounter();
        assertEquals(0, atomicCounter.get());
        atomicCounter.close();
        exceptionRule.expect(PrimitiveException.class);
        exceptionRule.expectMessage("counter not found");
        assertEquals(0, atomicCounter.get());
    }

    private AtomicCounter buildAtomicCounter() {
        return new DefaultAtomicCounterBuilder(PRIMITIVE_NAME, channel, EXECUTOR).build();
    }

    // Mock implementation of the counter server
    private final CounterGrpc.CounterImplBase counterImplBase = mock(CounterGrpc.CounterImplBase.class, delegatesTo(
        new CounterGrpc.CounterImplBase() {

            private AtomicLong atomicLong;

            @Override
            public void create(CreateRequest request, StreamObserver<CreateResponse> responseObserver) {
                atomicLong = new AtomicLong(0);
                responseObserver.onNext(CreateResponse.newBuilder().build());
                responseObserver.onCompleted();
            }

            @Override
            public void close(CloseRequest request, StreamObserver<CloseResponse> responseObserver) {
                atomicLong = null;
                responseObserver.onNext(CloseResponse.newBuilder().build());
                responseObserver.onCompleted();
            }

            @Override
            public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
                if (atomicLong == null) {
                    responseObserver.onError(Status.NOT_FOUND.withDescription("counter not found...")
                        .asRuntimeException());
                } else {
                    responseObserver.onNext(GetResponse.newBuilder()
                        .setValue(atomicLong.get())
                        .build());
                    responseObserver.onCompleted();
                }
            }

            @Override
            public void set(SetRequest request, StreamObserver<SetResponse> responseObserver) {
                if (atomicLong == null) {
                    responseObserver.onError(Status.NOT_FOUND.withDescription("counter not found...")
                        .asRuntimeException());
                } else {
                    atomicLong.set(request.getValue());
                    responseObserver.onNext(SetResponse.newBuilder()
                        .setValue(atomicLong.get())
                        .build());
                    responseObserver.onCompleted();
                }
            }

            @Override
            public void update(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {
                if (atomicLong == null) {
                    responseObserver.onError(Status.NOT_FOUND.withDescription("counter not found...")
                        .asRuntimeException());
                } else {
                    if (atomicLong.compareAndSet(request.getCheck(), request.getUpdate())) {
                        responseObserver.onNext(UpdateResponse.newBuilder()
                            .setValue(request.getUpdate())
                            .build());
                        responseObserver.onCompleted();
                    } else {
                        responseObserver.onError(Status.ABORTED.withDescription("optimistic lock failure")
                            .asRuntimeException());
                    }
                }
            }

            @Override
            public void increment(IncrementRequest request, StreamObserver<IncrementResponse> responseObserver) {
                if (atomicLong == null) {
                    responseObserver.onError(Status.NOT_FOUND.withDescription("counter not found...")
                        .asRuntimeException());
                } else {
                    long value = atomicLong.addAndGet(request.getDelta());
                    responseObserver.onNext(IncrementResponse.newBuilder()
                        .setValue(value)
                        .build());
                    responseObserver.onCompleted();
                }
            }

            @Override
            public void decrement(DecrementRequest request, StreamObserver<DecrementResponse> responseObserver) {
                if (atomicLong == null) {
                    responseObserver.onError(Status.NOT_FOUND.withDescription("counter not found...")
                        .asRuntimeException());
                } else {
                    long value = atomicLong.decrementAndGet();
                    responseObserver.onNext(DecrementResponse.newBuilder()
                        .setValue(value)
                        .build());
                    responseObserver.onCompleted();
                }
            }

        }
    ));

}
