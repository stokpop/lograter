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
package nl.stokpop.lograter.counter;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.store.TimeMeasurement;
import nl.stokpop.lograter.store.TimeMeasurementStore;
import nl.stokpop.lograter.store.TimeMeasurementStoreInMemory;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class RequestCounter extends Counter implements Iterable<TimeMeasurement> {

    private static final Logger log = LoggerFactory.getLogger(RequestCounter.class);

	public static final RequestCounter EMPTY_REQUEST_COUNTER = new RequestCounter(CounterKey.of("EMPTY"), new TimeMeasurementStoreInMemory());

	private final TimeMeasurementStore timeMeasurements;
	private final boolean fixedTimePeriod;
	private final TimePeriod timePeriod;

	// used to "grow" a TimePeriod via adding measurements when no fixedTimePeriod is given
	private long firstTimestamp = TimePeriod.NOT_SET;
	private long lastTimestamp = TimePeriod.NOT_SET;

	public RequestCounter(CounterKey key, TimeMeasurementStore timeMeasurementStore) {
		this(key, timeMeasurementStore, TimePeriod.UNDEFINED_PERIOD);
	}

	public RequestCounter(CounterKey key, TimeMeasurementStore timeMeasurementStore, TimePeriod timePeriod) {
		super(key);
//		if (!timePeriod.hasBothTimestampsSet()) {
//			throw new LogRaterException(String.format("Not allowed to create a RequestCounter with unset TimePeriod [%s]", timePeriod));
//		}
		this.timeMeasurements = timeMeasurementStore;
		this.timePeriod = timePeriod;
		this.fixedTimePeriod = timePeriod.hasBothTimestampsSet();
	}

	/**
	 * Create new request counter based on the two given request counters.
	 * This is an InMemory request counter.
	 */
	public RequestCounter(final RequestCounter one, final RequestCounter two, final TimePeriod timePeriod) {
		this(CounterKey.merge(one.getUniqueCounterKey(), two.getUniqueCounterKey()), one, two, timePeriod);
	}

	/**
	 * Create new request counter based on the two given request counters for the given time period.
	 * This is an InMemory request counter.
	 */
	public RequestCounter(final CounterKey key, final RequestCounter one, final RequestCounter two, final TimePeriod timePeriod) {
		this(key, new TimeMeasurementStoreInMemory(), timePeriod);

		if (timePeriod.isUndefined()) {
            for (TimeMeasurement timeMeasurement : one) {
                addTimeMeasurement(timeMeasurement);
            }
            for (TimeMeasurement timeMeasurement : two) {
                addTimeMeasurement(timeMeasurement);
            }
        }
        else {
            final RequestCounter sliceOne = one.getTimeSlicedCounter(timePeriod);
            final RequestCounter sliceTwo = two.getTimeSlicedCounter(timePeriod);

            for (TimeMeasurement timeMeasurement : sliceOne) {
                addTimeMeasurement(timeMeasurement);
            }
            for (TimeMeasurement timeMeasurement : sliceTwo) {
                addTimeMeasurement(timeMeasurement);
            }
        }
	}

	/**
	 * Create new request counter based on the two given request counters. The time period will be combined time period
	 * of the two given counters.
	 * This is an InMemory request counter.
	 */
	public RequestCounter(final CounterKey key, final RequestCounter successCounter, final RequestCounter failureCounter) {
		this(key, successCounter, failureCounter,
				TimePeriod.createMaxTimePeriod(successCounter.getTimePeriod(), failureCounter.getTimePeriod()));
	}

	private void addTimeMeasurement(final TimeMeasurement timeMeasurement) {
		this.timeMeasurements.add(timeMeasurement);
		updateFirstAndLastTimestamp(timeMeasurement.getTimestamp());
	}

	public static String createCounterNameThatAlignsInTextReport(String totalCounterName, int additionalColumns) {
		StringBuilder totalCounterNameSB = new StringBuilder(totalCounterName);
		for (int i = 0; i < additionalColumns; i++) {
			totalCounterNameSB.append(",TOTAL");
		}

		return totalCounterNameSB.toString();
	}

	public void incRequests(long timestamp, int durationInMilliseconds) {
		if (timestamp < 0) {
			throw new LogRaterException(String.format("Timestamps cannot be below 0: [%d]", timestamp));
		}
		this.timeMeasurements.add(timestamp, durationInMilliseconds);
		updateFirstAndLastTimestamp(timestamp);
	}

	private void updateFirstAndLastTimestamp(long timestamp) {
		if (timestamp < this.firstTimestamp || this.firstTimestamp == TimePeriod.NOT_SET) {
			this.firstTimestamp = timestamp;
		}
		// add one millisecond, because end of TimePeriod is EXCLUDED and TimePeriod should be at least 1 ms
		if (timestamp >= this.lastTimestamp || this.lastTimestamp == TimePeriod.NOT_SET) {
			this.lastTimestamp  = timestamp + 1;
		}
	}

	/**
	 * @return iterator of TimeMeasurements ordered on timestamp
	 */
	@Override
	public Iterator<TimeMeasurement> iterator() {
		return timeMeasurements.iterator();
	}


	/**
	 * This will return only complete buckets.
	 * @see RequestCounter#fillReducedCounter
	 */
	public static void fillReducedCounter(RequestCounter fromCounter, RequestCounter toCounter, long aggregatePeriodInMillis) {
		fillReducedCounter(fromCounter, toCounter, aggregatePeriodInMillis, false);
	}

	/**
	 * Create one point with the avg of all durations in aggregation period.
	 * Also determine how many points were in each aggregation period.
	 * @param fromCounter counter to reduce
	 * @param toCounter empty (!) counter to fill with the reduced data points
	 * @param aggregatePeriodInMillis the duration of each aggregation, should be bigger than 0
	 * @param onlyCompleteBuckets skip remainder if not a complete full bucket
	 */
	public static void fillReducedCounter(RequestCounter fromCounter, RequestCounter toCounter, long aggregatePeriodInMillis, boolean onlyCompleteBuckets) {

	    if (fromCounter.getTimePeriod().equals(toCounter.getTimePeriod())) {
	        log.debug("From counter and to counter time periods are not equal, might have surprising effects... FROM[{}] TO[{}]", fromCounter, toCounter);
        }

        if (aggregatePeriodInMillis <= 0 ) {
	        throw new LogRaterException(String.format("Invalid aggregation period in millis, should be larger than 0: %d", aggregatePeriodInMillis));
        }

		TimePeriod timePeriod = fromCounter.getTimePeriod();
		long start = timePeriod.getStartTime();
		long end = timePeriod.getEndTime();

		long totalDurationInPeriod = 0;
		int hitsInPeriod = 0;
		int period = 1;
		long middleOfPeriod = start + (aggregatePeriodInMillis / 2);
		long endOfPeriod = start + aggregatePeriodInMillis;
        
		// walk through all items until last, so resource connections can be closed on last hasNext call that returns false
		for (TimeMeasurement tm : fromCounter) {

            // fast forward to the bucket where the first time measurement resides.
            while (hitsInPeriod == 0 && tm.getTimestamp() >= endOfPeriod) {
                period = period + 1;
                endOfPeriod = start + (period * aggregatePeriodInMillis);
                middleOfPeriod = endOfPeriod - (aggregatePeriodInMillis / 2);
            }

            // for all entries within current time period, calculate totals
			if (tm.getTimestamp() < endOfPeriod) {
				hitsInPeriod++;
				totalDurationInPeriod = totalDurationInPeriod + tm.getDurationInMillis();
			} else {
			    // end of a period, save the accumulated date for the previous period
			    toCounter.incRequests(middleOfPeriod, (int) (totalDurationInPeriod / hitsInPeriod));
			    // start a new period
                hitsInPeriod = 1;
                totalDurationInPeriod = tm.getDurationInMillis();
                // fast forward to the bucket where the next time measurement resides.
                while (tm.getTimestamp() >= endOfPeriod) {
                    period = period + 1;
                    endOfPeriod = start + (period * aggregatePeriodInMillis);
                    middleOfPeriod = endOfPeriod - (aggregatePeriodInMillis / 2);
                }
			}
		}
		
		// add last entry if at end of time period and if incomplete buckets are allowed
		if (end == endOfPeriod || (hitsInPeriod != 0 && !onlyCompleteBuckets)) {
			toCounter.incRequests(middleOfPeriod, (int) (totalDurationInPeriod / hitsInPeriod));
		}
	}

	public RequestCounter  getTimeSlicedCounter(TimePeriod timePeriod) throws LogRaterException {
		if (!timePeriod.hasBothTimestampsSet()) {
			throw new LogRaterException(String.format("Cannot create time sliced counter without timestamps [%s]", timePeriod));
		}

		TimeMeasurementStore timeSlicedTimeMeasurements = timeMeasurements.getTimeSlice(timePeriod);

		return new RequestCounterReadOnly(getCounterKey(), timeSlicedTimeMeasurements, timePeriod);
	}

	public CounterKey getUniqueCounterKey() {
		return getCounterKey();
	}

	public long getHits() {
		return timeMeasurements.getSize();
	}

	public boolean isEmpty() { return timeMeasurements.isEmpty(); }

	@Override
	public String toString() {
		return "RequestCounter{name=" + super.getCounterKey() +
				", timeMeasurements=" + timeMeasurements +
				", timePeriod=" + getTimePeriod() +
				"} ";
	}

	public TimePeriod getTimePeriod() {
		if (fixedTimePeriod) {
			return timePeriod;
		}
		else if (firstTimestamp == TimePeriod.NOT_SET && lastTimestamp == TimePeriod.NOT_SET) {
			return TimePeriod.UNDEFINED_PERIOD;
		}
		else {
			return TimePeriod.createExcludingEndTime(firstTimestamp, lastTimestamp);
		}
	}

	/**
	 * Safe sliced counter deals with null values for counters and checks time period.
	 * @param counter the counter to slice
	 * @param timePeriod the time period for the slice
	 * @return the sliced counter
	 */
	public static RequestCounter safeSlicedCounter(RequestCounter counter, TimePeriod timePeriod) {
		if (counter == null) {
			counter = EMPTY_REQUEST_COUNTER;
		}
		else {
			if (timePeriod.hasBothTimestampsSet() && !timePeriod.covers(counter.getTimePeriod())) {
				counter = counter.getTimeSlicedCounter(timePeriod);
			}
		}
		return counter;
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

}
