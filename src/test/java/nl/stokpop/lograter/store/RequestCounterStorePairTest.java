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

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterStorageType;
import org.junit.Test;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RequestCounterStorePairTest {

    @Test
    public void addAndOverflow() {

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
        RequestCounterStore storeSuccess = factory.newInstance("success", "successStore", 1);
        RequestCounterStore storeFailure = factory.newInstance("failure", "failureStore", 1);

        RequestCounterStorePair pair = new RequestCounterStorePair(storeSuccess, storeFailure);

        pair.addSuccess("key1", 0, 10);
        pair.addSuccess("key2", 1, 11);
        pair.addSuccess("key3", 2, 12);
        pair.addFailure("key4", 3, 13);
        pair.addFailure("key1", 4, 14);

        // success and failure should be in balance
        RequestCounterStore successFromPair = pair.getRequestCounterStoreSuccess();
        assertEquals(3, successFromPair.getTotalRequestCounter().getHits());
        assertEquals(2, successFromPair.getCounterKeys().size());
        assertEquals(2, successFromPair.get(OVERFLOW_COUNTER).getHits());
        assertEquals(1, successFromPair.get("key1").getHits());
        assertTrue("successes should overflow", successFromPair.isOverflowing());

        RequestCounterStore failureFromPair = pair.getRequestCounterStoreFailure();
        assertEquals(2, failureFromPair.getTotalRequestCounter().getHits());
        assertEquals(1, failureFromPair.get(OVERFLOW_COUNTER).getHits());
        assertEquals(1, failureFromPair.get("key1").getHits());
        assertNull("should not contain key4 after overflow", failureFromPair.get("key4"));
        assertTrue("failures should overflow", failureFromPair.isOverflowing());

    }

    @Test
    public void emptyOverflowInFailuresExpected() {

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
        RequestCounterStore storeSuccess = factory.newInstance("success", "successStore", 1);
        RequestCounterStore storeFailure = factory.newInstance("failure", "failureStore", 1);

        RequestCounterStorePair pair = new RequestCounterStorePair(storeSuccess, storeFailure);

        pair.addSuccess("key1", 0, 10);
        pair.addSuccess("key2", 1, 11);
        pair.addSuccess("key3", 2, 12);

        // success and failure should be in balance
        RequestCounterStore successFromPair = pair.getRequestCounterStoreSuccess();
        assertEquals(3, successFromPair.getTotalRequestCounter().getHits());
        assertEquals(2, successFromPair.getCounterKeys().size());
        assertEquals(2, successFromPair.get(OVERFLOW_COUNTER).getHits());
        assertEquals(1, successFromPair.get("key1").getHits());
        assertTrue("successes should overflow", successFromPair.isOverflowing());

        RequestCounterStore failureFromPair = pair.getRequestCounterStoreFailure();
        assertEquals(0, failureFromPair.getTotalRequestCounter().getHits());
        assertNotNull("empty overflow expected (all successes should have matching failure counter)", failureFromPair.get(OVERFLOW_COUNTER));
        assertEquals(0, failureFromPair.get(OVERFLOW_COUNTER).getHits());
        assertNotNull("should contain key1", failureFromPair.get("key1"));
        assertNull("should not contain key2", failureFromPair.get("key2"));
        assertTrue("failures should overflow", failureFromPair.isOverflowing());

    }

    @Test(expected = LogRaterException.class)
    public void differentSizedMaxCounterStores() {

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);
        RequestCounterStore storeSuccess = factory.newInstance("success", "successStore", 2);
        RequestCounterStore storeFailure = factory.newInstance("failure", "failureStore", 1);

        // should throw exception: max unique counters should be equal
        new RequestCounterStorePair(storeSuccess, storeFailure);

    }

}