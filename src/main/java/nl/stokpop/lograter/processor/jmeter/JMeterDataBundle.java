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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.counter.RequestCounterDataBundle;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.linemapper.LineMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bundles all jmeter log data.
 */
public class JMeterDataBundle implements RequestCounterDataBundle {

    private final static Logger log = LoggerFactory.getLogger(JMeterDataBundle.class);

    private final RequestCounterStorePair totalRequestCounterPair;
    private final List<RequestCounterStorePair> requestCounterStorePairs;
    private final JMeterConfig config;
    private final Map<String, LineMap> counterKeyToLineMapMap;

    public JMeterDataBundle(JMeterConfig config,
                            List<RequestCounterStorePair> requestCounterStorePairs,
                            RequestCounterStorePair totalRequestCounterStorePair,
                            Map<String, LineMap> counterKeyToLineMapMap) {

        if (counterKeyToLineMapMap == null) {
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
        this.counterKeyToLineMapMap = counterKeyToLineMapMap;

        this.config = config;
    }

    public JMeterDataBundle(JMeterConfig config, List<RequestCounterStorePair> requestCounterStores, RequestCounterStorePair totalRequestCounterPair) {
        this(config,
		        requestCounterStores, totalRequestCounterPair,
		        Collections.emptyMap());
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

    @Override
    public BasicCounterLogConfig getConfig() {
        return config;
    }


    public Map<String, LineMap> getCounterKeyToLineMapMap() {
        return counterKeyToLineMapMap;
    }

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("AccessLogDataBundle{");
		sb.append("totalRequestCounterPair=").append(totalRequestCounterPair);
		sb.append(", requestCounterStorePairs=").append(requestCounterStorePairs);
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
