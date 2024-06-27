/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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

import nl.stokpop.lograter.clickpath.ClickPath;
import nl.stokpop.lograter.clickpath.ClickPathCollector;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.counter.SimpleCounter;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.linemapper.LineMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bundles all access log data.
 */
public class AccessLogDataBundle implements RequestCounterDataBundle {

    private static final Logger log = LoggerFactory.getLogger(AccessLogDataBundle.class);

    public static final ClickPathCollector NOOP_CLICK_PATH_COLLECTOR = new ClickPathCollector() {
        @Override
        public void addClickPath(ClickPath clickPath) {
            log.debug("Add clickpath called op NOOP click path collector for {}", clickPath);
        }

        @Override
        public Map<String, SimpleCounter> getClickPaths() {
            return Collections.emptyMap();
        }

        @Override
        public String getExampleSessionIdForClickPath(String path) {
            return "NOOP IMPLEMENTATION - no example session id";
        }

        @Override
        public long getAvgSessionDurationForClickPath(String path) {
            return 0;
        }

        @Override
        public String getPathAsStringWithAvgDuration(String path) {
            return "NOOP IMPLEMENTATION - no path as String available";
        }

        @Override
        public long getClickPathLength(String path) {
            return 0;
        }
    };

    private final RequestCounterStorePair totalRequestCounterPair;
    private final List<RequestCounterStorePair> requestCounterStorePairs;
    private final ClickPathCollector clickPathCollector;
    private final AccessLogConfig config;
    private final Map<CounterKey, LineMap> counterKeyToLineMapMap;

    public AccessLogDataBundle(AccessLogConfig config,
                               List<RequestCounterStorePair> requestCounterStorePairs,
                               RequestCounterStorePair totalRequestCounterStorePair,
                               ClickPathCollector clickPathCollector,
                               Map<CounterKey, LineMap> keyToLineMap) {

        if (keyToLineMap == null) {
            throw new NullPointerException("counterKeyToLineMapMap cannot be null");
        }
        if (requestCounterStorePairs == null) {
            throw new NullPointerException("requestCounterStorePairs cannot be null");
        }
        if (totalRequestCounterStorePair == null) {
            throw new NullPointerException("totalRequestCounterStorePair cannot be null");
        }

        this.totalRequestCounterPair = totalRequestCounterStorePair;
        this.requestCounterStorePairs = requestCounterStorePairs;
        this.clickPathCollector = clickPathCollector;
        this.counterKeyToLineMapMap = keyToLineMap;

        this.config = config;
    }

    public AccessLogDataBundle(AccessLogConfig config, List<RequestCounterStorePair> requestCounterStores, RequestCounterStorePair totalRequestCounterPair) {
        this(config,
		        requestCounterStores, totalRequestCounterPair,
		        Collections.emptyMap());
    }

    public AccessLogDataBundle(AccessLogConfig config,
                               List<RequestCounterStorePair> requestCounterStorePairs, RequestCounterStorePair totalRequestCounterPair,
                               Map<CounterKey, LineMap> keyToLineMap) {
        this(config,
		        requestCounterStorePairs, totalRequestCounterPair,
		        NOOP_CLICK_PATH_COLLECTOR, keyToLineMap);
    }

	@Override
	public RequestCounterStorePair getTotalRequestCounterStorePair() {
		return totalRequestCounterPair;
	}

	@Override
	public List<RequestCounterStorePair> getRequestCounterStorePairs() {
		return requestCounterStorePairs;
	}

	@Override
	public boolean doesSupportFailureRequestCounters() {
		return true;
	}

	/**
     * Can return the NOOP_CLICK_PATH_COLLECTOR if no real clickpath collector is present.
     */
    public ClickPathCollector getClickPathCollector() {
        return clickPathCollector;
    }

    @Override
    public AccessLogConfig getConfig() {
        return config;
    }

    public Map<CounterKey, LineMap> getKeyToLineMap() {
        return counterKeyToLineMapMap;
    }

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("AccessLogDataBundle{");
		sb.append("totalRequestCounterPair=").append(totalRequestCounterPair);
		sb.append(", requestCounterStorePairs=").append(requestCounterStorePairs);
		sb.append(", clickPathCollector=").append(clickPathCollector);
		sb.append(", config=").append(config);
		sb.append(", counterKeyToLineMapMap=").append(counterKeyToLineMapMap);
		sb.append('}');
		return sb.toString();
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
