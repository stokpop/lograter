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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.List;

public class LatencyLogProcessor implements Processor<LatencyLogEntry> {

	private static final char SEP_CHAR = ',';

	private final LatencyLogData data;
	private final LatencyLogConfig config;
	private final List<String> counterFields;

	public LatencyLogProcessor(LatencyLogData data, LatencyLogConfig config) {
		this.data = data;
		this.config = config;
		this.counterFields = config.getCounterFields();
	}

    @Override
	public void processEntry(LatencyLogEntry entry) {
		
		String counterKey = createCounterKey(entry);

		TimePeriod filterPeriod = config.getFilterPeriod();
		if (!filterPeriod.isWithinTimePeriod(entry.getTimestamp())) {
			data.incFilteredLines();
			return;
		}
		
		long timestamp = entry.getTimestamp();
		
		data.updateLogTime(timestamp);

		int durationInMillis = entry.getDurationInMillis();
		if (entry.isSuccess()) {
			data.getCounterStorePair().addSuccess(counterKey, timestamp, durationInMillis);
		}
		else {
			data.getCounterStorePair().addFailure(counterKey, timestamp, durationInMillis);
		}

	}

    private String createCounterKey(LatencyLogEntry entry) {
		StringBuilder perfCounterName = new StringBuilder();
		for (String field : counterFields) {
			perfCounterName.append(entry.getField(field)).append(SEP_CHAR);
		}
		return perfCounterName.substring(0, perfCounterName.length() - 1);
	}

	public LatencyLogData getData() {
		return data;
	}

	public LatencyLogConfig getConfig() {
		return config;
	}

}
