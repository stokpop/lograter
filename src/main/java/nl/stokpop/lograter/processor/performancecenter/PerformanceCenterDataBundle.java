/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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

import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;

import java.util.Collections;
import java.util.List;

/**
 * Bundles all performance center data.
 */
public class PerformanceCenterDataBundle implements RequestCounterDataBundle {
    private final PerformanceCenterConfig config;
    private final PerformanceCenterData performanceCenterData;

    public PerformanceCenterDataBundle(PerformanceCenterConfig config, PerformanceCenterData data) {
        this.performanceCenterData = data;
        this.config = config;
    }

	@Override
	public RequestCounterStorePair getTotalRequestCounterStorePair() {
		return performanceCenterData.getRequestCounterStorePair();
	}

	@Override
	public List<RequestCounterStorePair> getRequestCounterStorePairs() {
		return Collections.singletonList(performanceCenterData.getRequestCounterStorePair());
	}

	@Override
	public boolean doesSupportFailureRequestCounters() {
		return true;
	}

	public PerformanceCenterConfig getConfig() {
        return config;
    }

    public PerformanceCenterAggregationGranularity getAggregationGranularity() {
    	return performanceCenterData.getPcAggregationGranularity();
	}

    @Override
    public String toString() {
        return "PerformanceLogDataBundle{" +
                "config=" + config +
                ", performanceCenterData=" + performanceCenterData +
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
