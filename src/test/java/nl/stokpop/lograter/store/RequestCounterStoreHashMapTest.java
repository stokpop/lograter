/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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

import org.junit.Test;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreHashMapTest {

    @Test
    public void addWithMaxUniqueCounters() {
        RequestCounterStore store = new RequestCounterStoreHashMap("testStore", "totalRequests");
        RequestCounterStore storeMax = new RequestCounterStoreMaxCounters(store, 1);

        storeMax.add("key1", 1, 1);
        storeMax.add("key2", 2, 2);
        storeMax.add("key3", 2, 2);
        storeMax.add("key4", 2, 2);
        storeMax.add("key1", 2, 2);

        assertEquals("key1 and overflow counter expected", 2, storeMax.getCounterKeys().size());
        assertEquals("two hits on key1 expected", 2, storeMax.get("key1").getHits());
        assertEquals("overflow should have 3 hits", 3, storeMax.get(OVERFLOW_COUNTER).getHits());
    }
}