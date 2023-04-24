/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Wraps a RequestCounterStore to limit the number of unique counters.
 */
public class RequestCounterStoreMaxCounters implements RequestCounterStore {

    public static final String OVERFLOW_COUNTER_NAME = "OVERFLOW-COUNTER";

    private final RequestCounterStore store;

    private final int maxUniqueCounters;

    RequestCounterStoreMaxCounters(RequestCounterStore store, int maxUniqueCounters) {
        this.store = store;
        this.maxUniqueCounters = maxUniqueCounters;
    }

    @Override
    public String getName() {
        return store.getName();
    }

    @Override
    public Set<CounterKey> getCounterKeys() {
        return Collections.unmodifiableSet(store.getCounterKeys());
    }

    @Override
    public RequestCounter get(CounterKey key) {
        return store.get(key);
    }

    @Override
    public RequestCounter getTotalRequestCounter() {
        return store.getTotalRequestCounter();
    }

    @Override
    public boolean contains(CounterKey key) {
        return false;
    }

    @Override
    public void add(CounterKey counterKey, long timestamp, int durationMillis) {
        RequestCounter currentCounter;
        if (isOverflowing()) {
            currentCounter = findCounterWhenOverflown(counterKey);
        }
        else {
            currentCounter = addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(counterKey);
        }
        currentCounter.incRequests(timestamp, durationMillis);
        store.getTotalRequestCounter().incRequests(timestamp, durationMillis);
    }

    private RequestCounter findCounterWhenOverflown(CounterKey counterKey) {
        RequestCounter currentCounter;
        if (store.getCounterKeys().contains(counterKey)) {
            currentCounter = store.get(counterKey);
        }
        else {
            currentCounter = addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(counterKey);
        }
        return currentCounter;
    }

    @Override
    public boolean isOverflowing() {
        return store.getCounterKeys().size() >= maxUniqueCounters;
    }

    @Override
    public boolean isEmpty() {
        return store.isEmpty();
    }

    @Override
    public RequestCounter addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(CounterKey counterKey) {
        if (isOverflowing()) {
            CounterKey counterKeyWithFieldsInName = CounterKey.createCounterKeyWithFieldsInName(OVERFLOW_COUNTER_NAME, counterKey.getMetaData());
            return store.addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(counterKeyWithFieldsInName);
        }
        else {
            return store.addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(counterKey);
        }
    }

    @Override
    public Iterator<RequestCounter> iterator() {
        return store.iterator();
    }

    public int getMaxUniqueCounters() {
        return maxUniqueCounters;
    }
    
    @Override
    public String toString() {
        return "RequestCounterStoreMaxCounters{" + "store=" + store +
                ", maxUniqueCounters=" + maxUniqueCounters +
                '}';
    }
}
