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
package nl.stokpop.lograter.counter;

import nl.stokpop.lograter.store.TimeMeasurementStore;
import nl.stokpop.lograter.util.time.TimePeriod;

/**
 * A request counter that cannot be changed.
 */
public class RequestCounterReadOnly extends RequestCounter {

	/**
	 * Create new (in memory!) counter based on two request counters for the given time period.
	 * The returned RequestCounter is read only.
	 */
	public RequestCounterReadOnly(
			final RequestCounter requestCounterOne,
			final RequestCounter requestCounterTwo,
			final TimePeriod timePeriod) {
		super(requestCounterOne, requestCounterTwo, timePeriod);
	}

	/**
	 * Create new (in memory!) counter based on two request counters for the given time period.
	 * The returned RequestCounter is read only.
	 */
	public RequestCounterReadOnly(
			final CounterKey counterKey,
			final RequestCounter requestCounterOne,
			final RequestCounter requestCounterTwo,
			final TimePeriod timePeriod) {
		super(counterKey, requestCounterOne, requestCounterTwo, timePeriod);
	}

	public RequestCounterReadOnly(final CounterKey key, final TimeMeasurementStore timeMeasurementStore) {
		super(key, timeMeasurementStore);
	}

	protected RequestCounterReadOnly(final CounterKey key, final TimeMeasurementStore timeSlicedTimeMeasurements, final TimePeriod timePeriod) {
		super(key, timeSlicedTimeMeasurements, timePeriod);
	}

	public RequestCounterReadOnly(final CounterKey uniqueKey, final RequestCounter successCounter, final RequestCounter failureCounter) {
		super(uniqueKey, successCounter, failureCounter);
	}

	@Override
	public void incRequests(final long timestamp, final int durationInMilliseconds) {
		throw new UnsupportedOperationException("This request counter is read only.");
	}
	
}
