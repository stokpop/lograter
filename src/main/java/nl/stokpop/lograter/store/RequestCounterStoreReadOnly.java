/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.stokpop.lograter.store;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class RequestCounterStoreReadOnly implements RequestCounterStore {

    private final RequestCounterStore store;

    public RequestCounterStoreReadOnly(RequestCounterStore store) {
        this.store = store;
    }

    public String getName() {
        return store.getName();
    }

    public Set<CounterKey> getCounterKeys() {
        return store.getCounterKeys();
    }

    public RequestCounter get(CounterKey key) {
        return store.get(key);
    }

    public RequestCounter getTotalRequestCounter() {
        return store.getTotalRequestCounter();
    }

    public boolean contains(CounterKey key) {
        return store.contains(key);
    }

    public void add(CounterKey counterKey, long timestamp, int duration) {
        throw new UnsupportedOperationException("This RequestCounterStore is read only.");
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }
    
    public RequestCounter addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(CounterKey counterKey) {
        throw new UnsupportedOperationException("This RequestCounterStore is read only.");
    }

    public Iterator<RequestCounter> iterator() {
        return store.iterator();
    }

    @Override
    public void forEach(Consumer<? super RequestCounter> action) {
        store.forEach(action);
    }

    @Override
    public Spliterator<RequestCounter> spliterator() {
        return store.spliterator();
    }

    @Override
    public boolean isOverflowing() {
        return store.isOverflowing();
    }

    @Override
    public String toString() {
        return "RequestCounterStoreReadOnly{" + "store=" + store + '}';
    }
}
