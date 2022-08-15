/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimeMeasurementStoreToFilesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testAdd() {
		TimeMeasurementStoreToFiles store = new TimeMeasurementStoreToFiles(temporaryFolder.getRoot(), "Test-Store", "Test-External-Merge", 10);
		long totalDuration = 0;

		int loop = 1000;
		for (int i = 0; i < loop; i++) {
			// add two values per timestamp, to check for edge cases in iterator next check
			int duration = (int) (Math.random() * 100);
			long timestamp = 10000000 + (int) (Math.random() * 100);
			store.add(timestamp, duration);
			store.add(timestamp, duration);
			totalDuration = totalDuration + (2 * duration);
		}

		long checkTotal = getCheckTotalAndOrderOfStore(store);

		assertEquals("The size should equal two times loop value.", loop * 2, store.getSize());
		assertEquals("Total should be sum of durations", totalDuration, checkTotal);

	}

	@Test(expected = Throwable.class)
	public void storeNamesMustBeUnique() {
		TimeMeasurementStoreToFiles store1 = new TimeMeasurementStoreToFiles(temporaryFolder.getRoot(),"Test-Store-1", "Test-External-Merge-Same", 10);
		TimeMeasurementStoreToFiles store2 = new TimeMeasurementStoreToFiles(temporaryFolder.getRoot(),"Test-Store-1", "Test-External-Merge-Same", 10);
	}

	@Test
	public void twoStoresDoNotCrash() {

		TimeMeasurementStoreToFiles store1 = new TimeMeasurementStoreToFiles(temporaryFolder.getRoot(),"Test-Store-1", "Test-External-Merge-1", 10);
		long totalDuration1 = 0;
		long totalDuration2 = 0;

		int loop = 200;
		for (int i = 0; i < loop; i++) {
			int duration = (int) (Math.random() * 100);
			long timestamp = 10000000 + (int) (Math.random() * 100);
			store1.add(timestamp, duration);
			totalDuration1 = totalDuration1 + duration;
		}

		TimeMeasurementStoreToFiles store2 = new TimeMeasurementStoreToFiles(temporaryFolder.getRoot(),"Test-Store-1", "Test-External-Merge-2", 10);

		for (int i = 0; i < loop; i++) {
			int duration = (int) (Math.random() * 100);
			long timestamp = 10000000 + (int) (Math.random() * 100);
			store2.add(timestamp, duration);
			totalDuration2 = totalDuration2 + duration;
		}

		long checkTotal1 = getCheckTotalAndOrderOfStore(store1);
		long checkTotal2 = getCheckTotalAndOrderOfStore(store2);

		assertEquals("The size should equal one time loop value.", loop, store1.getSize());
		assertEquals("The size should equal one time loop value.", loop, store1.getSize());
		assertEquals("Total should be sum of durations", totalDuration1, checkTotal1);
		assertEquals("Total should be sum of durations", totalDuration2, checkTotal2);

	}

	private long getCheckTotalAndOrderOfStore(TimeMeasurementStoreToFiles store1) {
		long checkTotal = 0;
		long previousTimestamp = 0;
		for (TimeMeasurement timeMeasurement : store1) {
			checkTotal = checkTotal + timeMeasurement.getDurationInMillis();
			assertTrue(previousTimestamp <= timeMeasurement.getTimestamp());
			previousTimestamp = timeMeasurement.getTimestamp();
		}
		return checkTotal;
	}

}