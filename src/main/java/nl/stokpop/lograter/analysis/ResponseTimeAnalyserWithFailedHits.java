/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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

import nl.stokpop.lograter.counter.RequestCounterPair;
import nl.stokpop.lograter.util.time.TimePeriod;

/**
 * Analyser for response times that also knows about failures.
 *
 * If supplied with RequestCounterPair with 'includeFailuresInAnalysis' set to true (default setting) then:
 * <ul>
 *     <li>The metrics (min, max, percentile, etc...) are calculated for failures and successes combined.</li>
 *     <li>The hits is total of successes and failures.</li>
 * </ul>
 *
 * <p>
 * If supplied with RequestCounterPair with 'includeFailuresInAnalysis' set to false then:
 * <ul>
 *     <li>The metrics (min, max, percentile, etc...) are calculated for successes only.</li>
 *     <li>The hits is total of successes only.</li>
 *     <li>For percentage failure calculation the total hits is success hits + failure hits.</li>
 * </ul>
 */
public class ResponseTimeAnalyserWithFailedHits extends ResponseTimeAnalyser implements FailureAware {

	private final long numberOfFailureHits;
	private final boolean includeFailuresInAnalysis;

	/**
	 * Analyse the request counter for the total time period of success and failure counters.
	 */
	public ResponseTimeAnalyserWithFailedHits(RequestCounterPair pair) {
		super(pair.getCombinedRequestCounter());
		// safe to take all failure hits now: this is based on total time period of both counters
		this.numberOfFailureHits = pair.getCounterFailure().getHits();
		this.includeFailuresInAnalysis = pair.isIncludeFailuresInAnalysis();
	}

	/**
	 * Analyse the request counter for the specified time period.
	 */
	public ResponseTimeAnalyserWithFailedHits(RequestCounterPair pair, TimePeriod timePeriod) {
		super(pair.getCombinedRequestCounter(), timePeriod);
		// make sure to only get the failures for the provided time period
		this.numberOfFailureHits = pair.getCounterFailure().getTimeSlicedCounter(timePeriod).getHits();
        this.includeFailuresInAnalysis = pair.isIncludeFailuresInAnalysis();
	}

	@Override
    public double failurePercentage() {
	    // when not including failures in analysis, the totalHits is only the successes,
        // so the failures need to be added for a correct percentage calculation.
	    long totalHits = includeFailuresInAnalysis ? totalHits() : totalHits() + numberOfFailureHits;
		return (numberOfFailureHits / (totalHits + Double.MIN_VALUE)) * 100d;
	}

	@Override
    public long failedHits() {
		return numberOfFailureHits;
	}

}
