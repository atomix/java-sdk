package io.atomix.election;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * State of leadership for an election.
 * <p>
 * Provided by this construct is the current leader and the list of {@link T candidateIds}s
 * currently registered as candidates for election for a term.
 * Keep in mind that only registered candidates can become leaders.
 */
public class Leadership<T> {
    private final long term;
    private final T leader;
    private final List<T> candidates;

    public Leadership(long term, T leader, List<T> candidates) {
        this.term = term;
        this.leader = leader;
        this.candidates = ImmutableList.copyOf(candidates);
    }

    /**
     * Returns the leadership term.
     *
     * @return the leadership term
     */
    public long term() {
        return term;
    }

    /**
     * Returns the current leader for the term.
     *
     * @return leader; will be null if there is no leader for the term
     */
    public T leader() {
        return leader;
    }

    /**
     * Returns an preference-ordered list of nodes that are in the leadership
     * race for this topic.
     *
     * @return a list of NodeIds in priority-order, or an empty list.
     */
    public List<T> candidates() {
        return candidates;
    }

    /**
     * Maps the leadership identifiers using the given mapper.
     *
     * @param mapper the mapper with which to convert identifiers
     * @param <U>    the converted identifier type
     * @return the converted leadership
     */
    public <U> Leadership<U> map(Function<T, U> mapper) {
        return new Leadership<>(
            term,
            leader != null ? mapper.apply(leader) : null,
            candidates.stream().map(mapper).collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, leader, candidates);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Leadership) {
            final Leadership other = (Leadership) obj;
            return this.term == other.term
                       && Objects.equals(this.leader, other.leader)
                       && Objects.equals(this.candidates, other.candidates);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                   .add("term", term)
                   .add("leader", leader)
                   .add("candidates", candidates)
                   .toString();
    }
}
