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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.processor.CounterKeyCreator;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.store.RequestCounterStorePair;

public class JMeterLogCounterProcessor implements Processor<JMeterLogEntry> {

	private final RequestCounterStorePair counterStorePair;
    private final CounterKeyCreator<JMeterLogEntry> counterKeyCreator;

    public JMeterLogCounterProcessor(RequestCounterStorePair counterStorePair, CounterKeyCreator<JMeterLogEntry> counterKeyCreator) {
		this.counterStorePair = counterStorePair;
        this.counterKeyCreator = counterKeyCreator;
	}

	@Override
	public void processEntry(final JMeterLogEntry logEntry) {

		long timestamp = logEntry.getTimestamp();
		int durationInMillis = logEntry.getDurationInMillis();

		String counterKey = counterKeyCreator.createCounterKey(logEntry);

		if (logEntry.isSuccess()) {
            counterStorePair.addSuccess(counterKey, timestamp, durationInMillis);
		}
		else {
            counterStorePair.addFailure(counterKey, timestamp, durationInMillis);
		}
	}

	public RequestCounterStorePair getCounterStorePair() {
		return counterStorePair;
	}

}
