/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
import org.junit.Test;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER_NAME;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreHashMapTest {

    @Test
    public void addWithMaxUniqueCounters() {
        RequestCounterStore store = new RequestCounterStoreHashMap("testStore", CounterKey.of("totalRequests"));
        RequestCounterStore storeMax = new RequestCounterStoreMaxCounters(store, 1);


        storeMax.add(CounterKey.of("key1"), 1, 1);
        storeMax.add(CounterKey.of("key2"), 2, 2);
        storeMax.add(CounterKey.of("key3"), 2, 2);
        storeMax.add(CounterKey.of("key4"), 2, 2);
        storeMax.add(CounterKey.of("key1"), 2, 2);

        assertEquals("key1 and overflow counter expected", 2, storeMax.getCounterKeys().size());
        assertEquals("two hits on key1 expected", 2, storeMax.get(CounterKey.of("key1")).getHits());
        assertEquals("overflow should have 3 hits", 3, storeMax.get(CounterKey.of(OVERFLOW_COUNTER_NAME)).getHits());
    }
}