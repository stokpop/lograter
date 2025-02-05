/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.DatabaseBootstrapTest;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER_NAME;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreSqLiteTest {

    @Test
    public void add() {
        DatabaseBootstrapTest.injectTestDatabasePathIntoSysVars();

        DatabaseBootstrap.instance().bootstrapDatabase(true);

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Database, TimePeriod.MAX_TIME_PERIOD);
        RequestCounterStore testStore = factory.newInstance("testStore", CounterKey.of("all-requests"), 1);

        testStore.add(CounterKey.of("key1"), 1,1);
        testStore.add(CounterKey.of("key2"), 2,2);
        testStore.add(CounterKey.of("key3"), 3,3);
        testStore.add(CounterKey.of("key4"), 4,4);
        testStore.add(CounterKey.of("key1"), 5,5);

        assertEquals("expect key1 and OVERFLOW_COUNTER (and not all-requests)", 2, testStore.getCounterKeys().size());
        assertEquals("expect 2 hits on key1", 2, testStore.get(CounterKey.of("key1")).getHits());
        assertEquals("expect 3 hits on OVERFLOW_COUNTER", 3, testStore.get(CounterKey.of(OVERFLOW_COUNTER_NAME)).getHits());
        assertEquals("expect 5 hits in total", 5, testStore.getTotalRequestCounter().getHits());
    }
}