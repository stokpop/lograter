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

import nl.stokpop.lograter.counter.CounterStorageType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static nl.stokpop.lograter.store.RequestCounterStoreMaxCounters.OVERFLOW_COUNTER;
import static org.junit.Assert.assertEquals;

public class RequestCounterStoreExternalSortTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testMax1Unique() {

        final int cap = 1;

        // in order to avoid memory issues, there should be a cap on the number of request counters per counter store
        RequestCounterStore store =  new RequestCounterStoreFactory(CounterStorageType.ExternalSort, temporaryFolder.getRoot())
                .newInstance("mappers-success", "myTestStore", cap);


        store.add("my-counter-" + 1, 0, 0);
        store.add("my-counter-" + 2, 2, 2);
        store.add("my-counter-" + 3, 3, 3);

        // 1 unique keys and one overflow key with 2 entries
        assertEquals(2, store.getCounterKeys().size());
        assertEquals(3, store.getTotalRequestCounter().getHits());
        assertEquals(2, store.get(OVERFLOW_COUNTER).getHits());
    }

}