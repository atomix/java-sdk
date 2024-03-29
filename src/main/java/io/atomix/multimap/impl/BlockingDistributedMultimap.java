package io.atomix.multimap.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.atomix.Cancellable;
import io.atomix.Synchronous;
import io.atomix.collection.DistributedCollection;
import io.atomix.collection.impl.BlockingDistributedCollection;
import io.atomix.map.impl.BlockingDistributedMap;
import io.atomix.multimap.AsyncDistributedMultimap;
import io.atomix.map.DistributedMap;
import io.atomix.multimap.DistributedMultimap;
import io.atomix.multimap.MultimapEventListener;
import io.atomix.multiset.DistributedMultiset;
import io.atomix.set.DistributedSet;
import io.atomix.multiset.impl.BlockingDistributedMultiset;
import io.atomix.set.impl.BlockingDistributedSet;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link DistributedMultimap} providing synchronous access to {@link AsyncDistributedMultimap}.
 */
public class BlockingDistributedMultimap<K, V>
    extends Synchronous<DistributedMultimap<K, V>, AsyncDistributedMultimap<K, V>>
    implements DistributedMultimap<K, V> {

    private final AsyncDistributedMultimap<K, V> asyncMultimap;

    public BlockingDistributedMultimap(AsyncDistributedMultimap<K, V> asyncMultimap, Duration operationTimeout) {
        super(asyncMultimap, operationTimeout);
        this.asyncMultimap = asyncMultimap;
    }

    @Override
    public int size() {
        return complete(asyncMultimap.size());
    }

    @Override
    public boolean isEmpty() {
        return complete(asyncMultimap.isEmpty());
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return complete(asyncMultimap.containsKey((K) key));
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return complete(asyncMultimap.containsValue((V) value));
    }

    @Override
    public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
        return complete(asyncMultimap.containsEntry((K) key, (V) value));
    }

    @Override
    public boolean put(@Nullable K key, @Nullable V value) {
        return complete(asyncMultimap.put(key, value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        return complete(asyncMultimap.remove((K) key, (V) value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
        return complete(asyncMultimap.putAll(key, values instanceof Collection ? (Collection<V>) values : Lists.newArrayList(values)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        return complete(asyncMultimap.putAll((Map<K, Collection<? extends V>>) (Map) multimap.asMap()));
    }

    @Override
    public boolean putAll(Map<K, Collection<? extends V>> mappings) {
        return complete(asyncMultimap.putAll(mappings));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
        return complete(asyncMultimap.replaceValues(key, values instanceof Collection ? (Collection<V>) values : Lists.newArrayList(values)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> removeAll(@Nullable Object key) {
        return complete(asyncMultimap.removeAll((K) key));
    }

    @Override
    public boolean removeAll(Map<K, Collection<? extends V>> mappings) {
        return complete(asyncMultimap.removeAll(mappings));
    }

    @Override
    public void clear() {
        complete(asyncMultimap.clear());
    }

    @Override
    public Collection<V> get(@Nullable K key) {
        return complete(asyncMultimap.get(key));
    }

    @Override
    public DistributedSet<K> keySet() {
        return new BlockingDistributedSet<>(asyncMultimap.keySet(), operationTimeout);
    }

    @Override
    public DistributedMultiset<K> keys() {
        return new BlockingDistributedMultiset<>(asyncMultimap.keys(), operationTimeout);
    }

    @Override
    public DistributedMultiset<V> values() {
        return new BlockingDistributedMultiset<>(asyncMultimap.values(), operationTimeout);
    }

    @Override
    public DistributedCollection<Map.Entry<K, V>> entries() {
        return new BlockingDistributedCollection<>(asyncMultimap.entries(), operationTimeout);
    }

    @Override
    public DistributedMap<K, Collection<V>> asMap() {
        return new BlockingDistributedMap<>(asyncMultimap.asMap(), operationTimeout);
    }

    @Override
    public Cancellable listen(MultimapEventListener<K, V> listener, Executor executor) {
        return complete(asyncMultimap.listen(listener, executor));
    }

    @Override
    public AsyncDistributedMultimap<K, V> async() {
        return asyncMultimap;
    }
}
