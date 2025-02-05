/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.linemapper.LineMap;

import java.util.List;
import java.util.Map;

/**
 * Bundles all latency log data.
 */
public class LatencyLogDataBundle implements RequestCounterDataBundle {
    private final LatencyLogConfig config;
    private final LatencyLogData data;
    private final ClickPathCollector clickPathCollector;
	private final List<RequestCounterStorePair> requestCounterStorePairs;
	private final Map<CounterKey, LineMap> keyToLineMap;

    public LatencyLogDataBundle(LatencyLogConfig config, LatencyLogData data, List<RequestCounterStorePair> requestCounterStorePairs, ClickPathCollector clickPathCollector, Map<CounterKey, LineMap> keyToLineMap) {
        // TODO: why both data and requestCounterStorePairs?
		// seems data contains non-mapped and requestCounterStorePairs contain mapped counter fields?
    	this.config = config;
	    this.data = data;
        this.clickPathCollector = clickPathCollector;
        this.requestCounterStorePairs = requestCounterStorePairs;
		this.keyToLineMap = keyToLineMap;
	}

    public LatencyLogDataBundle(LatencyLogConfig config, LatencyLogData data, List<RequestCounterStorePair> requestCounterStorePairs, Map<CounterKey, LineMap> keyToLineMap) {
       this(config, data, requestCounterStorePairs, null, keyToLineMap);
    }

	public Map<CounterKey, LineMap> getKeyToLineMap() {
		return keyToLineMap;
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
		return requestCounterStorePairs;
	}

	@Override
	public boolean doesSupportFailureRequestCounters() {
		return config.isFailureAwareAnalysis();
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
