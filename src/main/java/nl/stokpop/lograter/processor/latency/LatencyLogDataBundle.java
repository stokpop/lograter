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

import nl.stokpop.lograter.clickpath.ClickPathCollector;
import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;

import java.util.Collections;
import java.util.List;

/**
 * Bundles all latency log data.
 */
public class LatencyLogDataBundle implements RequestCounterDataBundle {
    private final LatencyLogConfig config;
    private final LatencyLogData data;
    private final ClickPathCollector clickPathCollector;

    public LatencyLogDataBundle(LatencyLogConfig config, LatencyLogData data, ClickPathCollector clickPathCollector) {
        this.config = config;
	    this.data = data;
        this.clickPathCollector = clickPathCollector;
    }

    public LatencyLogDataBundle(LatencyLogConfig config, LatencyLogData data) {
       this(config, data, null);
    }

    public ClickPathCollector getClickPathCollector() {
        return clickPathCollector;
    }

	@Override
	public RequestCounterStorePair getTotalRequestCounterStorePair() {
		return data.getCounterStorePair();
	}

	@Override
	public List<RequestCounterStorePair> getRequestCounterStorePairs() {
		return Collections.singletonList(data.getCounterStorePair());
	}

	@Override
	public boolean doesSupportFailureRequestCounters() {
		return false;
	}

	public LatencyLogConfig getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "LatencyLogDataBundle{" +
                "config=" + config +
                ", LatencyLogData=" + data +
                ", clickPathCollector=" + clickPathCollector +
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
