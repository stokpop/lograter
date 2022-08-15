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
package nl.stokpop.lograter.counter;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFactory;
import nl.stokpop.lograter.store.TimeMeasurement;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class RequestCounterTest {

	@Test
	public void testBasicCalculations() {
        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());

		for (int i = 0; i < 100; i++) {
			counter.incRequests(100_000_000 + i, i + 1);
		}

		ResponseTimeAnalyser analyser = ResponseTimeAnalyserFactory.createSimpleFailureUnaware(counter);

		assertEquals(100, counter.getHits());
		assertEquals(50, analyser.maxConcurrentRequests().maxConcurrentRequests);
		assertEquals(99, analyser.percentileHitDuration(99));
		assertEquals(29.01d, analyser.stdDevHitDuration(), 0.01d);
	}

	@Test
	public void testBasicCalculationsBig() {
        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());

		final int loops = 10_000;
		
		for (int i = 0; i < loops; i++) {
			counter.incRequests(100_000_000 + i, i + 1);
		}

		ResponseTimeAnalyser analyser = ResponseTimeAnalyserFactory.createSimpleFailureUnaware(counter);
		
		assertEquals(loops, counter.getHits());
		assertEquals(5000, analyser.maxConcurrentRequests().maxConcurrentRequests);
		assertEquals(9900, analyser.percentileHitDuration(99));
		assertEquals(2886.9d, analyser.stdDevHitDuration(), 0.3d);
	}
	
	@Test 
	public void testSd() {

        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());

		// http://www.mathsisfun.com/data/standard-deviation.html
		counter.incRequests(1, 600);
		counter.incRequests(2, 470);
		counter.incRequests(3, 170);
		counter.incRequests(4, 430);
		counter.incRequests(5, 300);

		ResponseTimeAnalyser analyser = ResponseTimeAnalyserFactory.createSimpleFailureUnaware(counter);

		// note that this is the Sample sd and not the Population sd!
		assertEquals(164.0, analyser.stdDevHitDuration(), 1.0);
	}
	
	@Test 
	public void testPercentileSmallNumber() {

	    RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());

		counter.incRequests(1, 600);

		ResponseTimeAnalyser analyser = ResponseTimeAnalyserFactory.createSimpleFailureUnaware(counter);
		
		assertEquals(600, analyser.percentileHitDuration(99));
	}
	
	@Test 
	public void testTimeSlice() {

        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());

		counter.incRequests(0, 100);
		counter.incRequests(1, 100);
		counter.incRequests(2, 100);
		counter.incRequests(3, 100);
		counter.incRequests(4, 100);
		counter.incRequests(5, 100);

		// including timestamp 1, excluding timestamp 4
		RequestCounter timeslice = counter.getTimeSlicedCounter(TimePeriod.createExcludingEndTime(1, 4));
		assertEquals(3, timeslice.getHits());
		
	}

	@Test
	public void testFillReducedCounter() {
        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());

		counter.incRequests(10, 100);
		counter.incRequests(20, 50);
		counter.incRequests(110, 100);
		counter.incRequests(120, 50);
		counter.incRequests(170, 20);

        RequestCounter toCounter = new RequestCounter(CounterKey.of("toCounter"), new TimeMeasurementStoreInMemory());
		RequestCounter.fillReducedCounter(counter, toCounter, 40, false);

		assertEquals(3, toCounter.getHits());

	}

	@Test
	public void testFillReducedCounterCount() {
        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());

		IntStream.rangeClosed(1, 8).forEach(i -> counter.incRequests(i, 1));

        RequestCounter toCounter = new RequestCounter(CounterKey.of("toCounter"), new TimeMeasurementStoreInMemory());
		RequestCounter.fillReducedCounter(counter, toCounter, 4, true);

		assertEquals(2, toCounter.getHits());
	}

	@Test
	public void testFillReducedCounterEmpty() {
        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());
        RequestCounter toCounter = new RequestCounter(CounterKey.of("toCounter"), new TimeMeasurementStoreInMemory(), counter.getTimePeriod());

		RequestCounter.fillReducedCounter(counter, toCounter, 40);

		assertEquals(0, toCounter.getHits());
	}

	@Test
	public void testReducedCounterFirstPartEmpty() {
        TimePeriod timePeriod = TimePeriod.createIncludingEndTime(0, 20);
        RequestCounter fromCounter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory(), timePeriod);
        RequestCounter toCounter = new RequestCounter(CounterKey.of("toCounter"), new TimeMeasurementStoreInMemory(), timePeriod);

	    // test where there are several buckets without load, then with load, then without load
        IntStream.rangeClosed(10, 16).forEach(i -> fromCounter.incRequests(i, 100));

		RequestCounter.fillReducedCounter(fromCounter, toCounter, 2);

		assertEquals(4, toCounter.getHits());

		int[] expectedHits = {1, 1, 1, 1};
		int[] expectedTimestamps = {11, 13, 15, 17};
		int i = 0;
		for (TimeMeasurement tm : toCounter) {
            assertEquals(String.format("in bucket %d expected hits", i), expectedHits[i], tm.getNumberOfHits());
            assertEquals(String.format("in bucket %d expected timestamp", i), expectedTimestamps[i], tm.getTimestamp());
            i = i + 1;
        }
    }

	@Test
	public void testReducedCounterIntermediatePartEmpty() {
        TimePeriod timePeriod = TimePeriod.createIncludingEndTime(0, 20);
        RequestCounter fromCounter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory(), timePeriod);
        RequestCounter toCounter = new RequestCounter(CounterKey.of("toCounter"), new TimeMeasurementStoreInMemory(), timePeriod);

	    // test where there are several buckets without load, then with load, then without load
        IntStream.rangeClosed(4, 8).forEach(i -> fromCounter.incRequests(i, 100));
        IntStream.rangeClosed(14, 18).forEach(i -> fromCounter.incRequests(i, 100));

		RequestCounter.fillReducedCounter(fromCounter, toCounter, 2);

		assertEquals(6, toCounter.getHits());

		int[] expectedHits = {1, 1, 1, 1, 1, 1};
		int[] expectedTimestamps = {5, 7, 9, 15, 17, 19};
		int[] expectedDurations = {100, 100, 100, 100, 100, 100};
		int i = 0;
		for (TimeMeasurement tm : toCounter) {
            assertEquals(String.format("in bucket %d expected hits", i), expectedHits[i], tm.getNumberOfHits());
            assertEquals(String.format("in bucket %d expected timestamp", i), expectedTimestamps[i], tm.getTimestamp());
            assertEquals(String.format("in bucket %d expected duration", i), expectedDurations[i], tm.getDurationInMillis());
            i = i + 1;
		}
	}

	@Test(expected = LogRaterException.class)
	public void testFillReducedCounterZeroAggregationPeriod() {
        RequestCounter counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());
        RequestCounter toCounter = new RequestCounter(CounterKey.of("toCounter"), new TimeMeasurementStoreInMemory());
		RequestCounter.fillReducedCounter(counter, toCounter, 0, false);
	}

}
