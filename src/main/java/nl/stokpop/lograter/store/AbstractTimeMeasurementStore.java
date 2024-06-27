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

import nl.stokpop.lograter.util.time.TimePeriod;

/**
 * Adds time period management for the time measurement store.
 */
public abstract class AbstractTimeMeasurementStore implements TimeMeasurementStore {

	private long smallestTimestamp = 0;
	private long largestTimestamp = 1;

	protected void updateFirstAndLastTimestamps(long timestamp) {
		if (smallestTimestamp == 0 || smallestTimestamp > timestamp) {
			smallestTimestamp = timestamp;
		}
		if (largestTimestamp == 1 || largestTimestamp < timestamp) {
			largestTimestamp = timestamp;
		}
	}

	@Override
	public TimePeriod getTimePeriod() {
		return TimePeriod.createExcludingEndTime(smallestTimestamp, largestTimestamp);
	}

}
