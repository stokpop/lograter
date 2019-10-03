/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.DatabaseBootstrapTest;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NotThreadSafe
public class RequestCounterStoreTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
    public void testGet() {

        TimePeriod timePeriod = TimePeriod.createExcludingEndTime(0, 2000);
        RequestCounterStore counterStore = new RequestCounterStoreHashMap("MyCounterStore", "MyCounterStoreTotalRequestCounter", timePeriod);

        String myTestCounterKey = "MyTestCounter";
        counterStore.add(myTestCounterKey, 1000, 2000);
        RequestCounter myRequestCounter = counterStore.get(myTestCounterKey);

        String myTestCounterKey2 = "MyTestCounter2";
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

        String myTestCounterKey = "MyTestCounter";
        counterStore.add(myTestCounterKey, 1000, 2000);

        String myTestCounterKey2 = "MyTestCounter2";
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

}