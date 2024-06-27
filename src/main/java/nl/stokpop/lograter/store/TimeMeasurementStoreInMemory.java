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
package nl.stokpop.lograter.store;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is NOT thread safe due to sorting and sorting check.
 */
@NotThreadSafe
public class TimeMeasurementStoreInMemory extends AbstractTimeMeasurementStore {

	private final List<TimeMeasurement> timeMeasurements = new ArrayList<>();
	private boolean isOrdered = true;

	@Override
	public void add(long timestamp, int durationMillis) {
		this.add(new TimeMeasurement(timestamp, durationMillis));
	}

	@Override
	public TimeMeasurementStore getTimeSlice(TimePeriod timePeriod) {
		if (!isOrdered) {
			order();
		}
		return new TimeMeasurementStoreView(timePeriod, this);
	}

	@Override
	public long getSize() {
		return timeMeasurements.size();
	}

	@Override
	public void add(TimeMeasurement timeMeasurement) {
		long newTimestamp = timeMeasurement.getTimestamp();
		// check if stays ordered if already ordered
		if (isOrdered && !timeMeasurements.isEmpty()) {
			long lastTimestamp = timeMeasurements.get(timeMeasurements.size() - 1).getTimestamp();
			isOrdered = lastTimestamp <= newTimestamp;
		}
		timeMeasurements.add(timeMeasurement);
		updateFirstAndLastTimestamps(newTimestamp);
	}

	@Override
	public String toString() {
		return "TimeMeasurementStoreInMemory{" + "timeMeasurements.size=" + timeMeasurements.size() + ", isOrdered=" + isOrdered + '}';
	}

	@Override
	public TimeMeasurementIterator iterator() {

		if (!isOrdered) {
			order();
		}

		final Iterator<TimeMeasurement> iterator = timeMeasurements.iterator();

		return new TimeMeasurementIterator() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public TimeMeasurement next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				iterator.remove();

			}

			@Override
			public void close() {
				// NOOP
			}
		};
	}

	@Override
	public boolean isEmpty() {
		return timeMeasurements.isEmpty();
	}

	private void order() {
		timeMeasurements.sort(TimeMeasurement.ORDER_TIMESTAMP);
		isOrdered = true;
	}
}
