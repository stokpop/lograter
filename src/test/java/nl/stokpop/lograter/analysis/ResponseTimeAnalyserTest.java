/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.analysis;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import nl.stokpop.lograter.util.time.TPSMeasurement;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ResponseTimeAnalyserTest {

	private static final double LITTLE_DELTA = 0.000001d;
	private RequestCounter counter;
	
	@Before
	public void init() {
		this.counter = new RequestCounter(CounterKey.of("TestCounter"), new TimeMeasurementStoreInMemory());
	}

	@Test
	public void testPercentiles100() {
				
		for (int i = 1; i <= 100; i++) {
			counter.incRequests(i, i);
		}

		ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
		long[] percentiles = analyser.percentiles();
		assertEquals(1, percentiles[0]);
		assertEquals(50, percentiles[49]);
		assertEquals(99, percentiles[98]);
		assertEquals(100, percentiles[99]);
	}

	@Test
	public void testPercentilesFlat() {

		for (int i = 1; i <= 100; i++) {
			counter.incRequests(i, 50);
		}

		ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
		long[] percentiles = analyser.percentiles();

		assertEquals(50, percentiles[0]);
		assertEquals(50, percentiles[49]);
		assertEquals(50, percentiles[98]);
		assertEquals(50, percentiles[99]);
	}

	@Test
	public void testPercentiles10000() {
		
		for (int i = 1; i <= 10_000; i++) {
			counter.incRequests(i, i);
		}

		ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
		long[] percentiles = analyser.percentiles();
		assertEquals(100, percentiles[0]);
		assertEquals(5_000, percentiles[49]);
		assertEquals(10_000, percentiles[99]);
	}
	
	@Test 
	public void sameSecondTPS() {
		long timestamp = 100_000_000;
		int nrOfHits = 10_000;
		for (int i = 0; i < nrOfHits; i++) {
			// so 10_000 hits on one timestamp!
			counter.incRequests(timestamp, i);
		}

		ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
		long hitsInMinute = analyser.hitsInMinuteWithStartTime(timestamp);
		assertEquals(nrOfHits, hitsInMinute);
		
		TransactionCounterResult maxHitsPerSecond = analyser.maxHitsPerSecond();
		assertEquals(nrOfHits, maxHitsPerSecond.getMaxHitsPerDuration());
		// match on start of second
		assertEquals(timestamp, maxHitsPerSecond.getMaxHitsPerDurationTimestamp());

		TransactionCounterResult maxHitsPerMinute = analyser.maxHitsPerMinute();
		assertEquals(maxHitsPerMinute.getMaxHitsPerDuration(), nrOfHits);
		// match on start of second
		assertEquals(timestamp, maxHitsPerSecond.getMaxHitsPerDurationTimestamp());
	}

	@Test
    public void avgTPSforOneHit() {
        counter.incRequests(1000, 40);
        ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter);
        assertEquals(1, analyser.avgTps(), LITTLE_DELTA);
    }

    @Test
    public void avgTPSOverLongRun() {
        counter.incRequests(1000, 40);
		TimePeriod timePeriod = TimePeriod.createExcludingEndTime(0, 10000);
        ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(counter, timePeriod);
        assertEquals(0.1, analyser.avgTps(), LITTLE_DELTA);
    }

	@Test
	public void tpsInSlice() {
		long timestamp = 100_000_000;
		int nrOfHits = 100_000;
		for (int i = 0; i < nrOfHits; i++) {
			counter.incRequests(timestamp + i, i);
		}

		RequestCounter slice = counter.getTimeSlicedCounter(TimePeriod.createExcludingEndTime(timestamp + 10000, timestamp + 20000));
		ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(slice);
		TransactionCounterResult maxHitsPerSecond = analyser.maxHitsPerSecond();
		assertEquals(10000, slice.getHits());
		assertEquals(1000.0, maxHitsPerSecond.getMaxHitsPerDuration(), LITTLE_DELTA);
	}

	@Test
	public void tpsInSliceHalfFilled() {
		long timestamp = 100000000;
		int startHits = 5000;
		int endHits = 15000;
		for (int i = startHits; i < endHits; i++) {
			counter.incRequests(timestamp + i, i);
		}

		RequestCounter slice = counter.getTimeSlicedCounter(TimePeriod.createExcludingEndTime(timestamp + 10000, timestamp + 20000));
		ResponseTimeAnalyser analyser = new ResponseTimeAnalyserFailureUnaware(slice);
		TransactionCounterResult maxHitsPerSecond = analyser.maxHitsPerSecond();
		assertEquals(5000, slice.getHits());
		assertEquals(1000.0, maxHitsPerSecond.getMaxHitsPerDuration(), LITTLE_DELTA);
	}

	@Test
    public void emptyCounter() {
        RequestCounter emptyCounter = new RequestCounter(CounterKey.of("empty counter"), new TimeMeasurementStoreInMemory());
        try {
            new ResponseTimeAnalyserFailureUnaware(emptyCounter);
        } catch (LogRaterException e) {
            return;
        }
        fail("expected exception not thrown");
    }

    @Test
	public void findMaxHitsPerMinute() {
	    ResponseTimeAnalyser analyser = createTotalTestAnalyser();

	    TransactionCounterResult maxHitsPerMinute = analyser.maxHitsPerMinute();
	    assertEquals(8, maxHitsPerMinute.getMaxHitsPerDuration());
	    assertEquals(62000, maxHitsPerMinute.getMaxHitsPerDurationTimestamp());

	    long hitsInMinuteWithStartTime = analyser.hitsInMinuteWithStartTime(62000);
	    assertEquals(8, hitsInMinuteWithStartTime);
    }

	@Test
	public void checkTimebuckets() {

		// Expect alignment of the buckets on the minute with the highest TPS
		// (note that if you analyse multiple RequestCounters, such as total and its subs,
		// the alignment might be different, but it always finds the highest TPS per line.

		ResponseTimeAnalyser analyser = createTotalTestAnalyser();
		ResponseTimeAnalyser subAnalyser = createSubTestAnalyser();
		
		TimePeriod timePeriod = analyser.getCounter().getTimePeriod();

		TransactionCounterResult maxHitsPerMinute = analyser.maxHitsPerMinuteWithTpsMeasurements();
		List<TPSMeasurement> tpsPerTimestamp = maxHitsPerMinute.getTpsPerTimestamp();

		// because of alignment at 62000 ms, one extra bucket is expected that starts at 2000 ms
		assertEquals(4, tpsPerTimestamp.size());

		TPSMeasurement tpsMeasurement1 = tpsPerTimestamp.get(0);
		TPSMeasurement tpsMeasurement2 = tpsPerTimestamp.get(1);
		TPSMeasurement tpsMeasurement3 = tpsPerTimestamp.get(2);
		TPSMeasurement tpsMeasurement4 = tpsPerTimestamp.get(3);

		assertTrue("all measurements need to be after original TimePeriod start", tpsMeasurement1.getTimestamp() > timePeriod.getStartTime());
		assertTrue("all measurements need to be before original TimePeriod end", tpsMeasurement4.getTimestamp() < timePeriod.getEndTime());

		assertEquals(92000, tpsMeasurement1.getTimestamp());
		assertEquals(8.0d, tpsMeasurement1.getTps() * 60, LITTLE_DELTA);

		assertEquals(152000, tpsMeasurement2.getTimestamp());
		assertEquals(1.0d, tpsMeasurement2.getTps() * 60, LITTLE_DELTA);

		assertEquals(212000, tpsMeasurement3.getTimestamp());
		assertEquals(2.0d, tpsMeasurement3.getTps() * 60, LITTLE_DELTA);

		long hitsInMinute = subAnalyser.hitsInMinuteWithStartTime(maxHitsPerMinute.getMaxHitsPerDurationTimestamp());
		assertEquals(1, hitsInMinute);

		TransactionCounterResult subResult = subAnalyser.maxHitsPerMinuteWithTpsMeasurements();
		long maxHitsPerDuration = subResult.getMaxHitsPerDuration();
		assertEquals(3.0d, maxHitsPerDuration, LITTLE_DELTA);

	}

	private ResponseTimeAnalyser createTotalTestAnalyser() {
		// in first minute: 6 hits expected
		// in second minute: 3 hits expected
		// total hits is 9 hits
		// but max hits in one minute is 8 hits in the minute which starts at 2000 ms

		// >> bucket of minute 1 with 1 hit (start 2000)
		counter.incRequests(61000, 2);

		// >> bucket of minute 2 with 8 hits (start 52000) with highest TPS
		counter.incRequests(62000, 2);
		counter.incRequests(119001, 2);
		counter.incRequests(119002, 2);
		counter.incRequests(119003, 2);
		counter.incRequests(119004, 2);
		counter.incRequests(121001, 2);
		counter.incRequests(121002, 2);
		counter.incRequests(121003, 2);

		// >> bucket of minute 3 with 1 hit (start 122000)
		counter.incRequests(181999, 2);
		// >> bucket of minute 4 with 2 hits (start 182000)
		counter.incRequests(220000, 2);
		counter.incRequests(220010, 2);

		// >> bucket of minute 5 with 1 hit (start 242000)
		counter.incRequests(280010, 2);

		return new ResponseTimeAnalyserFailureUnaware(counter);
	}

	private ResponseTimeAnalyser createSubTestAnalyser() {
		RequestCounter subCounter = new RequestCounter(CounterKey.of("sub-counter"), new TimeMeasurementStoreInMemory(), TimePeriod.createExcludingEndTime(61000, 280010));

		subCounter.incRequests(119003, 2);

		subCounter.incRequests(181999, 2);
		subCounter.incRequests(220000, 2);
		subCounter.incRequests(220010, 2);

		return new ResponseTimeAnalyserFailureUnaware(subCounter);
	}

}
