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
package nl.stokpop.lograter.util.time;

import nl.stokpop.lograter.analysis.HistogramData;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class TimeWindowCalculatorTest {

    private static final double DELTA = 0.01;

    @Test
    public void testPercentileWithHighNumbers() {
        RequestCounter requestCounter = new RequestCounter(CounterKey.of("TestMe"), new TimeMeasurementStoreInMemory());

        // loop 10 times adding 100_000 range of numbers
        IntStream.rangeClosed(1, 10).forEach($ -> IntStream.rangeClosed(1, 100_000).forEach(i -> requestCounter.incRequests(i, i)));

        TimeWindowCalculator timeWindowCalculator = new TimeWindowCalculator("test", requestCounter);
        assertEquals("Check percentile", 95_000, timeWindowCalculator.determinePercentile(95.0d));
        assertEquals("Check percentile", 99_000, timeWindowCalculator.determinePercentile(99.0d));
        assertEquals("Check percentile", 99_500, timeWindowCalculator.determinePercentile(99.5d));
        assertEquals("Check percentile", 99_950, timeWindowCalculator.determinePercentile(99.95d));
        assertEquals("Check percentile", 99_995, timeWindowCalculator.determinePercentile(99.995d));
        assertEquals("Check percentile", 99_999, timeWindowCalculator.determinePercentile(99.999d));
        assertEquals("Check percentile", 100_000, timeWindowCalculator.determinePercentile(100.0d));
    }

    @Test
    public void testGetAverageDuration() {

	    RequestCounter requestCounter = new RequestCounter(CounterKey.of("TestMe"), new TimeMeasurementStoreInMemory());

	    int items = 10_000;
        IntStream.rangeClosed(1, items).forEach(i -> requestCounter.incRequests(i, i % 101));

        TimePeriod windowTimePeriod = TimePeriod.createExcludingEndTime(0, items);
        TimeWindowCalculator timeWindowCalculator = new TimeWindowCalculator("test", requestCounter, windowTimePeriod);
        assertEquals(50, timeWindowCalculator.getAverageDuration(), DELTA);
        assertEquals(50, timeWindowCalculator.determinePercentile(50));
        assertEquals(95, timeWindowCalculator.determinePercentile(95));
        assertEquals(99, timeWindowCalculator.determinePercentile(99));
        assertEquals(0, timeWindowCalculator.getMinDuration());
        assertEquals(100, timeWindowCalculator.getMaxDuration());
        assertEquals(1000, timeWindowCalculator.getAverageHitsPerSec(), DELTA);

	    HistogramData histogram = timeWindowCalculator.getHistogram(100, 0, 100);
	    assertEquals(0, histogram.getMin());
	    assertEquals(100, histogram.getMax());
	    assertEquals(1, histogram.getTimeRangeInMillis());
	    // TODO this is time ranges + 1??
	    assertEquals(101, histogram.getXvalues().length);

    }

	@Test
	public void testGetHistogram100() {

		RequestCounter requestCounter = new RequestCounter(CounterKey.of("TestMe"), new TimeMeasurementStoreInMemory());
		int items = 100;
		IntStream.range(0, items).forEach(i -> requestCounter.incRequests(i, i));

		TimePeriod windowTimePeriod = TimePeriod.createExcludingEndTime(0, items);
		TimeWindowCalculator timeWindowCalculator = new TimeWindowCalculator("test", requestCounter, windowTimePeriod);
		HistogramData histogram = timeWindowCalculator.getHistogram(1, 0, 100);
		assertEquals(0, histogram.getMin());
		assertEquals(99, histogram.getMax());
		assertEquals(100, histogram.getTimeRangeInMillis());
		assertEquals(1, histogram.getXvalues().length);

	}

}