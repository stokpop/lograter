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
 * The metrics and data of successes are reported, without the failures.
 *
 * The number of failures and failure percentage is present (but failures are <b>not</b> used in metrics calculations).
 *
 * The number of hits are the actual success hits (or passed requests).
 */
public class ResponseTimeAnalyserWithFailuresExcludedInMetrics extends ResponseTimeAnalyser implements FailureAware {

	private final long numberOfFailures;

    /**
     * Analyse the request counter for the total time period of success and failure counters.
     */
    public ResponseTimeAnalyserWithFailuresExcludedInMetrics(RequestCounterPair counterPair) {
        super(counterPair.getCounterSuccess(), TimePeriod.createMaxTimePeriod(counterPair.getCounterSuccess().getTimePeriod(), counterPair.getCounterFailure().getTimePeriod()));
        // safe to take all failure hits now:
        // this is based on total time period of both counters
        this.numberOfFailures = counterPair.getCounterFailure().getHits();
    }

    /**
     * Analyse the request counter for the specified time period.
     */
    public ResponseTimeAnalyserWithFailuresExcludedInMetrics(RequestCounterPair counterPair, TimePeriod timePeriod) {
        super(counterPair.getCounterSuccess(), timePeriod);
        // make sure to only get the failures for the provided time period
        this.numberOfFailures = counterPair.getCounterFailure().getTimeSlicedCounter(timePeriod).getHits();
    }

    public double failurePercentage() {
        // total hits of parent now excludes failure hits
        return (numberOfFailures / (totalHits() + numberOfFailures + Double.MIN_VALUE)) * 100d;
    }

    @Override
    public long failureHits() {
        return numberOfFailures;
    }

    @Override
    public long totalHits() {
        return super.totalHits();
    }


}
