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
package nl.stokpop.lograter.store;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.Iterator;

/**
 * This is a view on an underlying time measurements store, with TimePeriod boundaries.
 */
public class TimeMeasurementStoreView implements TimeMeasurementStore {

	private final TimeMeasurementStore innerStore;
	private final TimePeriod timePeriod;
	private final long size;

	/**
	 * A view on a bigger timemeasurements store for the given timePeriod.
	 * The add methods and getSlice methods will throw exceptions when used.
	 * @param timePeriod the iterator will only loop through this timePeriod and size will be of this timePeriod
	 * @param innerStore the backup timemeasurementstore, expected to be ordered on timestamp
	 */
	public TimeMeasurementStoreView(TimePeriod timePeriod, TimeMeasurementStore innerStore) {
		this.timePeriod = timePeriod;
		this.innerStore = innerStore;

		if (timePeriod.covers(this.innerStore.getTimePeriod())) {
			this.size = innerStore.getSize();
		}
		else {
			// TODO: this is heavy for external sort!
			// Maybe some binary search to find correct period? With random access file?
			final long endTime = timePeriod.getEndTime();
			int count = 0;
			for (TimeMeasurement tm : innerStore) {
				long tmTime = tm.getTimestamp();
				if (timePeriod.isWithinTimePeriod(tmTime)) {
					count++;
				}
				if (tmTime >= endTime) {
					break;
				}
			}
			this.size = count;
		}
	}

	@Override
	public void add(long timestamp, int durationMillis) {
		throw new LogRaterException("A TimeMeasurementStoreView is read only");
	}

	@Override
	public TimeMeasurementStore getTimeSlice(TimePeriod timePeriod) {
		return new TimeMeasurementStoreView(timePeriod, innerStore);
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public void add(TimeMeasurement timeMeasurement) {
		throw new LogRaterException("A TimeMeasurementStoreView is read only");
	}

	@Override
	public TimePeriod getTimePeriod() {
		return timePeriod;
	}


	@Override
	public String toString() {
		return "TimeMeasurementStoreView{" + "innerStore=" + innerStore + ", timePeriod=" + timePeriod + ", size=" + size + '}';
	}

	@Override
	public TimeMeasurementIterator iterator() {

		TimeMeasurement findFirstTimeMeasurementInPeriod = null;
		final Iterator<TimeMeasurement> iterator = innerStore.iterator();
		while (iterator.hasNext()) {
			final TimeMeasurement next = iterator.next();
			if (next.getTimestamp() >= timePeriod.getStartTime()) {
				findFirstTimeMeasurementInPeriod = next;
				break;
			}
		}

		final TimeMeasurement firstTimeMeasurementInPeriod = findFirstTimeMeasurementInPeriod;

		return new TimeMeasurementIterator() {

			public boolean hasNextCalled = false;
			boolean returnFirstTimeMeasurement = firstTimeMeasurementInPeriod != null;
			TimeMeasurement nextTimeMeasurement = firstTimeMeasurementInPeriod;

			@Override
			public boolean hasNext() {
				// no first was found in the whole time measurement store...
				if (firstTimeMeasurementInPeriod == null) return false;
				// return the first time measurement as next call to next()
				if (returnFirstTimeMeasurement) return true;
				if (iterator.hasNext() && !hasNextCalled) {
					hasNextCalled = true;
					nextTimeMeasurement = iterator.next();
				}
				else {
					return false;
				}
				return timePeriod.isWithinTimePeriod(nextTimeMeasurement.getTimestamp());
			}

			@Override
			public TimeMeasurement next() {
				TimeMeasurement currentTimemeasurement = nextTimeMeasurement;
				if (returnFirstTimeMeasurement) {
					returnFirstTimeMeasurement = false;
				}
				else {
					if (!hasNextCalled) {
						nextTimeMeasurement = iterator.next();
					}
				}
				hasNextCalled = false;
				return currentTimemeasurement;
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
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
