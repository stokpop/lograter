/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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

public interface TimeMeasurementStore extends Iterable<TimeMeasurement> {

	void add(long timestamp, int durationInMilliseconds);

	void add(TimeMeasurement timeMeasurement);

	TimePeriod getTimePeriod();

	TimeMeasurementStore getTimeSlice(TimePeriod timePeriod);

	long getSize();

	TimeMeasurementIterator iterator();

    boolean isEmpty();
}
