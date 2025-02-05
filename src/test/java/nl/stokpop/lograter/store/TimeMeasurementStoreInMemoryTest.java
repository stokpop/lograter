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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeMeasurementStoreInMemoryTest {

    @Test
    public void testAdd() {
        TimeMeasurementStoreInMemory timeMeasurementStoreInMemory = new TimeMeasurementStoreInMemory();
        timeMeasurementStoreInMemory.add(1000, 100);
        timeMeasurementStoreInMemory.add(2000, 200);

        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getStartTime());
        assertEquals(2000, timeMeasurementStoreInMemory.getTimePeriod().getEndTime());
        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getDurationInMillis());
    }

    @Test
    public void testAddTimeMeasurements() {
        TimeMeasurementStoreInMemory timeMeasurementStoreInMemory = new TimeMeasurementStoreInMemory();
        timeMeasurementStoreInMemory.add(new TimeMeasurement(1000, 100));
        timeMeasurementStoreInMemory.add(new TimeMeasurement(2000, 200));

        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getStartTime());
        assertEquals(2000, timeMeasurementStoreInMemory.getTimePeriod().getEndTime());
        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getDurationInMillis());
    }


}