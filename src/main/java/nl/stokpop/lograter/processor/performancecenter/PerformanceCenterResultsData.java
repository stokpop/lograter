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
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import nl.stokpop.lograter.util.time.DataTimePeriod;

public class PerformanceCenterResultsData extends PerformanceCenterData {

    private final DataTimePeriod dataTimePeriod = new DataTimePeriod();

    public PerformanceCenterResultsData(RequestCounterStoreFactory csFactory, PerformanceCenterAggregationGranularity granularity, int maxUniqueCounters) {
		super(csFactory, granularity, maxUniqueCounters);
    }

    public void addSuccess(CounterKey key, long timestamp, int duration) {
        dataTimePeriod.updateDataTime(timestamp);
        getRequestCounterStorePair().addSuccess(key, timestamp, duration);
    }

	public void addFailure(CounterKey key, long timestamp, int duration) {
		dataTimePeriod.updateDataTime(timestamp);
		getRequestCounterStorePair().addFailure(key, timestamp, duration);
	}

    @Override
    public String toString() {
        return "PerformanceCenterResultsData{" +
                "dataTimePeriod=" + dataTimePeriod +
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
