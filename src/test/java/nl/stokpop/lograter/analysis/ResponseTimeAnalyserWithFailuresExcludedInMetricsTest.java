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
package nl.stokpop.lograter.analysis;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.counter.RequestCounterPair;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResponseTimeAnalyserWithFailuresExcludedInMetricsTest {

	@Test
	public void countFailureHitsTotalPeriod() {
		RequestCounter successes = createSuccessCounter();
		RequestCounter failures = createFailureCounter();

		ResponseTimeAnalyserWithoutFailedHits analyser = new ResponseTimeAnalyserWithoutFailedHits(new RequestCounterPair(successes, failures));

		assertEquals("contain all hits successes", 4, analyser.totalHits());
		assertEquals(2, analyser.failedHits());
		assertEquals(2/6d * 100, analyser.failurePercentage(), 0.0000001d);
		assertEquals(20, analyser.min());
		assertEquals("duration is total duration of successes and failures",2501, analyser.getAnalysisTimePeriod().getDurationInMillis());
	}

	@Test
	public void countFailureHitsSuccessPeriod() {
		RequestCounter successes = createSuccessCounter();
		RequestCounter failures = createFailureCounter();

        ResponseTimeAnalyserWithoutFailedHits analyser = new ResponseTimeAnalyserWithoutFailedHits(new RequestCounterPair(successes, failures), successes.getTimePeriod());

		assertEquals("contain all hits of successes period", 4, analyser.totalHits());
		assertEquals(1, analyser.failedHits());
		assertEquals(1/5d * 100, analyser.failurePercentage(), 0.0000001d);
		assertEquals("duration is total duration of successes",2101, analyser.getAnalysisTimePeriod().getDurationInMillis());
	}

	private RequestCounter createFailureCounter() {
		RequestCounter failures = new RequestCounter(CounterKey.of("failures"), new TimeMeasurementStoreInMemory());
		failures.incRequests(2500, 1);
		failures.incRequests(3500, 1);
		return failures;
	}

	private RequestCounter createSuccessCounter() {
		RequestCounter successes = new RequestCounter(CounterKey.of("successes"), new TimeMeasurementStoreInMemory());
		successes.incRequests(1000, 20);
		successes.incRequests(2000, 20);
		successes.incRequests(3000, 20);
		successes.incRequests(3100, 20);
		return successes;
	}

}