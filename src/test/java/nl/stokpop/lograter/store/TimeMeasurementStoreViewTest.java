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

import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

public class TimeMeasurementStoreViewTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testIterator() {

		TimeMeasurementStore store = new TimeMeasurementStoreInMemory();

		for (int i = 0; i < 100; i++) {
			// add two values per timestamp, to check for edge cases in iterator next check
			store.add(i, i);
			store.add(i, i);
		}

		TimePeriod timePeriod = TimePeriod.createExcludingEndTime(10, 20);

		TimeMeasurementStoreView view = new TimeMeasurementStoreView(timePeriod, store);

		assertEquals("View size should be 20.", 10 * 2, view.getSize());

		int count = 0;
		int total = 0;
		for (TimeMeasurement timeMeasurement : view) {
			total = total + timeMeasurement.getDurationInMillis();
			count++;
		}

		assertEquals("Iterator should loop over 20 elements", 10 * 2, count);
		assertEquals("Total should be sum of durations", (10+11+12+13+14+15+16+17+18+19) * 2, total);

	}

	@Test
	public void testIteratorExternalSort() {

		TimeMeasurementStore store = new TimeMeasurementStoreToFiles(temporaryFolder.getRoot(), "store", "my-test", 10);

		for (int i = 0; i < 100; i++) {
			// add two values per timestamp, to check for edge cases in iterator next check
			store.add(i, i);
			store.add(i, i);
		}

		TimePeriod timePeriod = TimePeriod.createExcludingEndTime(10, 20);

		TimeMeasurementStoreView view = new TimeMeasurementStoreView(timePeriod, store);

		assertEquals("View size should be 20.", 10 * 2, view.getSize());

		int count = 0;
		int total = 0;
		for (TimeMeasurement timeMeasurement : view) {
			total = total + timeMeasurement.getDurationInMillis();
			count++;
		}

		assertEquals("Iterator should loop over 20 elements", 10 * 2, count);
		assertEquals("Total should be sum of durations", (10+11+12+13+14+15+16+17+18+19) * 2, total);

	}

}