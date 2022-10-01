package io.atomix.client.election.impl;

import io.atomix.client.Cancellable;
import io.atomix.client.DelegatingAsyncPrimitive;
import io.atomix.client.election.AsyncLeaderElection;
import io.atomix.client.election.Leader;
import io.atomix.client.election.LeaderElection;
import io.atomix.client.election.Leadership;
import io.atomix.client.election.LeadershipEvent;
import io.atomix.client.election.LeadershipEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Transcoding async atomic value.
 */
public class TranscodingAsyncLeaderElection<T1, T2>
    extends DelegatingAsyncPrimitive<AsyncLeaderElection<T1>, LeaderElection<T1>, AsyncLeaderElection<T2>>
    implements AsyncLeaderElection<T1> {
    private final AsyncLeaderElection<T2> backingElection;
    private final Function<T1, T2> identifierEncoder;
    private final Function<T2, T1> identifierDecoder;

    public TranscodingAsyncLeaderElection(AsyncLeaderElection<T2> backingElection, Function<T1, T2> identifierEncoder, Function<T2, T1> identifierDecoder) {
        super(backingElection);
        this.backingElection = backingElection;
        this.identifierEncoder = v -> v != null ? identifierEncoder.apply(v) : null;
        this.identifierDecoder = v -> v != null ? identifierDecoder.apply(v) : null;
    }

    @Override
    public CompletableFuture<Leadership<T1>> enter(T1 identifier) {
        return backingElection.enter(identifierEncoder.apply(identifier))
            .thenApply(leadership -> new Leadership<>(
                leadership.leader() == null ? null : new Leader<>(
                    identifierDecoder.apply(leadership.leader().id()),
                    leadership.leader().term(),
                    leadership.leader().timestamp()),
                leadership.candidates().stream().map(identifierDecoder).collect(Collectors.toList())));
    }

    @Override
    public CompletableFuture<Void> withdraw(T1 identifier) {
        return backingElection.withdraw(identifierEncoder.apply(identifier));
    }

    @Override
    public CompletableFuture<Boolean> anoint(T1 identifier) {
        return backingElection.anoint(identifierEncoder.apply(identifier));
    }

    @Override
    public CompletableFuture<Boolean> promote(T1 identifier) {
        return backingElection.promote(identifierEncoder.apply(identifier));
    }

    @Override
    public CompletableFuture<Void> evict(T1 identifier) {
        return backingElection.evict(identifierEncoder.apply(identifier));
    }

    @Override
    public CompletableFuture<Leadership<T1>> getLeadership() {
        return backingElection.getLeadership()
            .thenApply(leadership -> new Leadership<>(
                leadership.leader() == null ? null : new Leader<>(
                    identifierDecoder.apply(leadership.leader().id()),
                    leadership.leader().term(),
                    leadership.leader().timestamp()),
                leadership.candidates().stream().map(identifierDecoder).collect(Collectors.toList())));
    }

    @Override
    public CompletableFuture<Cancellable> listen(LeadershipEventListener<T1> listener, Executor executor) {
        return backingElection.listen(event -> new LeadershipEvent<>(
            LeadershipEvent.Type.CHANGE,
            new Leadership<>(
                event.newLeadership().leader() == null ? null : new Leader<>(
                    identifierDecoder.apply(event.newLeadership().leader().id()),
                    event.newLeadership().leader().term(),
                    event.newLeadership().leader().timestamp()),
                event.newLeadership().candidates().stream().map(identifierDecoder).collect(Collectors.toList())),
            event.oldLeadership() == null ? null : new Leadership<>(
                event.newLeadership().leader() == null ? null : new Leader<>(
                    identifierDecoder.apply(event.newLeadership().leader().id()),
                    event.newLeadership().leader().term(),
                    event.newLeadership().leader().timestamp()),
                event.newLeadership().candidates().stream().map(identifierDecoder).collect(Collectors.toList()))), executor);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("backingValue", backingElection)
            .toString();
    }
}
