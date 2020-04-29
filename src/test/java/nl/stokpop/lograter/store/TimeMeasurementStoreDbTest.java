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
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.assertEquals;

@NotThreadSafe
public class TimeMeasurementStoreDbTest {

	@Test
    public void testAdd() {
        DatabaseBootstrap.instance().bootstrapDatabase(true);
        Connection databaseConnection = DatabaseBootstrap.instance().getDatabaseConnection();
        TimeMeasurementStoreSqLite timeMeasurementStoreInMemory = new TimeMeasurementStoreSqLite("TestDB", 1, databaseConnection, TimePeriod.UNDEFINED_PERIOD);
        timeMeasurementStoreInMemory.add(1000, 100);
        timeMeasurementStoreInMemory.add(2000, 200);

        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getStartTime());
        assertEquals(2000, timeMeasurementStoreInMemory.getTimePeriod().getEndTime());
        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getDurationInMillis());
    }

    @Test
    public void testAddTimeMeasurements() {
        DatabaseBootstrap.instance().bootstrapDatabase(true);
        Connection databaseConnection = DatabaseBootstrap.instance().getDatabaseConnection();
        TimeMeasurementStoreSqLite timeMeasurementStoreInMemory = new TimeMeasurementStoreSqLite("TestDB", 1, databaseConnection, TimePeriod.UNDEFINED_PERIOD);
        timeMeasurementStoreInMemory.add(new TimeMeasurement(1000, 100));
        timeMeasurementStoreInMemory.add(new TimeMeasurement(2000, 200));

        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getStartTime());
        assertEquals(2000, timeMeasurementStoreInMemory.getTimePeriod().getEndTime());
        assertEquals(1000, timeMeasurementStoreInMemory.getTimePeriod().getDurationInMillis());
    }

	@Test
	public void testGetTimeSlicedTimeMeasurements() {
		DatabaseBootstrap.instance().bootstrapDatabase(true);
		Connection databaseConnection = DatabaseBootstrap.instance().getDatabaseConnection();
		TimeMeasurementStoreSqLite timeMeasurementStoreInDb = new TimeMeasurementStoreSqLite("TestDB", 1, databaseConnection, TimePeriod.UNDEFINED_PERIOD);
		timeMeasurementStoreInDb.add(new TimeMeasurement(1000, 100));
		timeMeasurementStoreInDb.add(new TimeMeasurement(2000, 200));
		timeMeasurementStoreInDb.add(new TimeMeasurement(3000, 300));
		timeMeasurementStoreInDb.add(new TimeMeasurement(4000, 400));

		TimePeriod timeSlicePeriod = TimePeriod.createExcludingEndTime(2000, 3000);
		TimeMeasurementStore timeSlice = timeMeasurementStoreInDb.getTimeSlice(timeSlicePeriod);


		assertEquals(2000, timeSlice.getTimePeriod().getStartTime());
		assertEquals(3000, timeSlice.getTimePeriod().getEndTime());
		assertEquals(2, timeSlice.getSize());
	}
}