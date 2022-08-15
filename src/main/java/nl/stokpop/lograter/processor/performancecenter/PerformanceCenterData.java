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
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.store.RequestCounterStore;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.store.RequestCounterStorePair;

public class PerformanceCenterData {

	private final PerformanceCenterAggregationGranularity pcAggregationGranularity;
	private final RequestCounterStorePair requestCounterStorePair;

	public PerformanceCenterData(RequestCounterStoreFactory csFactory, PerformanceCenterAggregationGranularity granularity, int maxUniqueCounters) {
		RequestCounterStore storeSuccess = csFactory.newInstance("performance-center-counter-store-success", maxUniqueCounters);
		RequestCounterStore storeFailure = csFactory.newInstance("performance-center-counter-store-failure", maxUniqueCounters);
		this.requestCounterStorePair = new RequestCounterStorePair(storeSuccess, storeFailure);
		this.pcAggregationGranularity = granularity;
	}

	public RequestCounterStorePair getRequestCounterStorePair() {
		return requestCounterStorePair;
	}

	public PerformanceCenterAggregationGranularity getPcAggregationGranularity() {
        return pcAggregationGranularity;
    }

	@Override
	public String toString() {
		return "PerformanceCenterData{" +
				"pcAggregationGranularity=" + pcAggregationGranularity +
				", requestCounterStorePair=" + requestCounterStorePair +
				'}';
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
