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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.time.TimePeriod;

public class LatencyLogProcessor implements Processor<LatencyLogEntry> {

	private final LatencyLogData data;
	private final LatencyLogConfig config;

	private final LatencyCounterKeyCreator keyCreator;

	public LatencyLogProcessor(LatencyLogData data, LatencyLogConfig config, LatencyCounterKeyCreator keyCreator) {
		this.data = data;
		this.config = config;
		this.keyCreator = keyCreator;
	}

    @Override
	public void processEntry(LatencyLogEntry entry) {

		CounterKey key = keyCreator.createCounterKey(entry);

		TimePeriod filterPeriod = config.getFilterPeriod();
		if (!filterPeriod.isWithinTimePeriod(entry.getTimestamp())) {
			data.incFilteredLines();
			return;
		}
		
		long timestamp = entry.getTimestamp();
		
		data.updateLogTime(timestamp);

		int durationInMillis = entry.getDurationInMillis();
		if (entry.isSuccess()) {
			data.getCounterStorePair().addSuccess(key, timestamp, durationInMillis);
		}
		else {
			data.getCounterStorePair().addFailure(key, timestamp, durationInMillis);
		}

	}

	public LatencyLogData getData() {
		return data;
	}

	public LatencyLogConfig getConfig() {
		return config;
	}

}
