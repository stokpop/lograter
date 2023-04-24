/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.processor.CounterKeyCreator;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.store.RequestCounterStorePair;

public class AccessLogCounterProcessor implements Processor<AccessLogEntry> {

	private final RequestCounterStorePair counterStorePair;
    private final CounterKeyCreator<AccessLogEntry> counterKeyCreator;

    public AccessLogCounterProcessor(RequestCounterStorePair counterStorePair, CounterKeyCreator<AccessLogEntry> counterKeyCreator) {
		this.counterStorePair = counterStorePair;
        this.counterKeyCreator = counterKeyCreator;
	}

	@Override
	public void processEntry(final AccessLogEntry logEntry) {

		long timestamp = logEntry.getTimestamp();
		int durationInMillis = logEntry.getDurationInMillis();

		CounterKey key = counterKeyCreator.createCounterKey(logEntry);

		if (logEntry.isHttpError()) {
			counterStorePair.addFailure(key, timestamp, durationInMillis);
		}
		else {
			counterStorePair.addSuccess(key, timestamp, durationInMillis);
		}

	}

	public RequestCounterStorePair getCounterStorePair() {
		return counterStorePair;
	}

}
