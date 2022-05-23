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

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.DatabaseBootstrapTest;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import java.util.stream.IntStream;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NotThreadSafe
public class RequestCounterStoreTest {

	@Test
    public void testGet() {

        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(0, 2000);
        RequestCounterStore counterStore = new RequestCounterStoreHashMap("MyCounterStore", CounterKey.of("MyCounterStoreTotalRequestCounter"), timePeriod);

        CounterKey myTestCounterKey = CounterKey.of("MyTestCounter");
        counterStore.add(myTestCounterKey, 1000, 2000);
        RequestCounter myRequestCounter = counterStore.get(myTestCounterKey);

        CounterKey myTestCounterKey2 = CounterKey.of("MyTestCounter2");
        counterStore.add(myTestCounterKey2, 2000, 4000);
        RequestCounter myRequestCounter2 = counterStore.get(myTestCounterKey2);

        assertNotNull(myRequestCounter);
        assertEquals(myRequestCounter.getHits(), 1);

        assertNotNull(myRequestCounter2);
        assertEquals(myRequestCounter2.getHits(), 1);

        RequestCounter myTotalRequestCounter = counterStore.getTotalRequestCounter();
        assertNotNull(myTotalRequestCounter);
        assertEquals(myTotalRequestCounter.getHits(), 2);
    }

	@Test
    public void testGetDb() {
	    DatabaseBootstrapTest.injectTestDatabasePathIntoSysVars();
	    
        DatabaseBootstrap.instance().bootstrapDatabase(true);

        RequestCounterStore counterStore = new RequestCounterStoreFactory(CounterStorageType.Database).newInstance("MyDbRequestStore");

        CounterKey myTestCounterKey = CounterKey.of("MyTestCounter");
        counterStore.add(myTestCounterKey, 1000, 2000);

        CounterKey myTestCounterKey2 = CounterKey.of("MyTestCounter2");
        counterStore.add(myTestCounterKey2, 2000, 4000);

        RequestCounter myRequestCounter = counterStore.get(myTestCounterKey);
        RequestCounter myRequestCounter2 = counterStore.get(myTestCounterKey2);

        assertNotNull(myRequestCounter);
        assertEquals(1, myRequestCounter.getHits());

        assertNotNull(myRequestCounter2);
        assertEquals(1, myRequestCounter2.getHits());

        RequestCounter myTotalRequestCounter = counterStore.getTotalRequestCounter();
        assertNotNull(myTotalRequestCounter);
        assertEquals(2, myTotalRequestCounter.getHits());
    }

    @Test
    public void testCreateTooManyRequestCounters() {

	    final int cap = 10;

	    // in order to avoid memory issues, there should be a cap on the number of request counters per counter store
        RequestCounterStore store =  new RequestCounterStoreFactory(CounterStorageType.Memory)
                .newInstance("mappers-success", CounterKey.of("myTestStore"), cap);

        final int max = cap + 13;

        IntStream.range(0, max).forEach(i -> store.add(CounterKey.of("my-counter-" + i), i, i));

        // 10 unique keys and one overflow key with 13 entries
        assertEquals(cap + 1, store.getCounterKeys().size());
        assertEquals(max, store.getTotalRequestCounter().getHits());
        assertEquals(13, store.get(OVERFLOW_COUNTER).getHits());
    }

    @Test
    public void testMax1Unique() {

        final int cap = 1;

        // in order to avoid memory issues, there should be a cap on the number of request counters per counter store
        RequestCounterStore store =  new RequestCounterStoreFactory(CounterStorageType.Memory)
                .newInstance("mappers-success", CounterKey.of("myTestStore"), cap);


        store.add(CounterKey.of("my-counter-" + 1), 0, 0);
        store.add(CounterKey.of("my-counter-" + 2), 2, 2);
        store.add(CounterKey.of("my-counter-" + 3), 3, 3);

        // 1 unique keys and one overflow key with 2 entries
        assertEquals(2, store.getCounterKeys().size());
        assertEquals(3, store.getTotalRequestCounter().getHits());
        assertEquals(2, store.get(OVERFLOW_COUNTER).getHits());
    }
}