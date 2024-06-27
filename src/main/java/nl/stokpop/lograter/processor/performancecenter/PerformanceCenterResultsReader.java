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
package nl.stokpop.lograter.processor.performancecenter;

import java.util.Map;

/**
 * Read performance center result data from a file.
 * Can be a database file (access log or sqlite). Possibly also a html report file.
 */
public interface PerformanceCenterResultsReader {

	PerformanceCenterResultsData readResultsData(int maxUniqueCounters);

	void addEventsToResultsData(EventMeter eventMeter, PerformanceCenterResultsData data, Map<Integer, PerformanceCenterEvent> eventMap, long testStartTimeInSecondsEpoch, long granularityInMillis);
}
