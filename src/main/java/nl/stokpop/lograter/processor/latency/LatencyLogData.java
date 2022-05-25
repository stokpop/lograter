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

import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.processor.BasicLogData;
import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;

public class LatencyLogData extends BasicLogData {
	private final RequestCounterStorePair counterStorePair;

	public LatencyLogData(RequestCounterStoreFactory csFactory, int maxUniqueCounters) {
		RequestCounterStore requestCounterStoreSuccess = csFactory.newInstance("latency-log-counterstore-success", maxUniqueCounters);
		RequestCounterStore requestCounterStoreFailure = csFactory.newInstance("latency-log-counterstore-failure", maxUniqueCounters);
		this.counterStorePair = new RequestCounterStorePair(requestCounterStoreSuccess, requestCounterStoreFailure);
	}
	
	public RequestCounterStorePair getCounterStorePair() {
		return this.counterStorePair;
	}

    /**
     * Gets reference to total request counter. Only one is expected.
     * If called before one add to the counter store is done, an exception is thrown.
     */
    public RequestCounter getTotalRequestCounter() {
        return counterStorePair.getRequestCounterStoreSuccess().getTotalRequestCounter();
    }

	@Override
	public String toString() {
		return "LatencyLogData{" +
				"counterStorePair=" + counterStorePair +
				"} " + super.toString();
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
