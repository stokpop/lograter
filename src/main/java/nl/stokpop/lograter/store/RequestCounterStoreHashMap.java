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
package nl.stokpop.lograter.store;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static nl.stokpop.lograter.command.AbstractCommandBasic.MAX_UNIQUE_COUNTERS;

@NotThreadSafe
public class RequestCounterStoreHashMap implements RequestCounterStore {

    public static final String OVERFLOW_COUNTER = "OVERFLOW-COUNTER";

    private final Map<String, RequestCounter> counters = new HashMap<>();
	private final String name;
	private final TimePeriod timePeriod;
    private final int maxUniqueCounters;
    private RequestCounter totalRequestCounter;

	RequestCounterStoreHashMap(String storeName, String totalRequestName, TimePeriod timePeriod, int maxUniqueCounters) {
		this.name = storeName;
		this.totalRequestCounter = new RequestCounter(totalRequestName, new TimeMeasurementStoreInMemory());
		this.timePeriod = timePeriod;
		this.maxUniqueCounters = maxUniqueCounters;
	}

	RequestCounterStoreHashMap(String storeName, String totalRequestsName, int maxUniqueCounters) {
		this(storeName, totalRequestsName, TimePeriod.MAX_TIME_PERIOD, maxUniqueCounters);
	}

	RequestCounterStoreHashMap(String storeName, String totalRequestsName) {
		this(storeName, totalRequestsName, TimePeriod.MAX_TIME_PERIOD, MAX_UNIQUE_COUNTERS);
	}

	public void add(String counterKey, long timestamp, int durationInMilliseconds) {
	    RequestCounter currentCounter;
	    if (isOverflown()) {
            currentCounter = determineCounterWhenOverflown(counterKey);
        }
	    else {
            currentCounter = addEmptyCounterIfNotExists(counterKey);
        }
	    currentCounter.incRequests(timestamp, durationInMilliseconds);
		totalRequestCounter.incRequests(timestamp, durationInMilliseconds);
	}

    private RequestCounter determineCounterWhenOverflown(String counterKey) {
        RequestCounter currentCounter;
        if (counters.containsKey(counterKey)) {
            currentCounter = counters.get(counterKey);
        }
        else {
            currentCounter = addEmptyCounterIfNotExists(OVERFLOW_COUNTER);
        }
        return currentCounter;
    }

    public boolean isOverflown() {
        return counters.size() >= maxUniqueCounters;
    }

    @Override
	public String toString() {
		return "RequestCounterStoreHashMap{" +
				"name='" + name + '\'' +
				", timePeriod=" + timePeriod +
				'}';
	}

	public Iterator<RequestCounter> iterator() {
		List<RequestCounter> values = new ArrayList<>(counters.values());
		Collections.sort(values);
		return values.iterator();
	}
	
	@Override
	public boolean isEmpty() {
		return counters.isEmpty();
	}

	@Override
	public RequestCounter addEmptyCounterIfNotExists(String counterKey) {
		if (!counters.containsKey(counterKey)) {
			RequestCounter counter = new RequestCounter(counterKey, new TimeMeasurementStoreInMemory());
			counters.put(counterKey, counter);
			return counter;
		}
		else {
			return counters.get(counterKey);
		}
	}

	@Override
	public RequestCounter get(String counterKey) {
		return counters.get(counterKey);
	}

	@Override
	public boolean contains(String counterKey) {
		return counters.containsKey(counterKey);
	}

	@Override
	public String getName() {
		return name;
	}

    @Override
    public List<String> getCounterKeys() {
        return new ArrayList<>(counters.keySet());
    }

	@Override
	public RequestCounter getTotalRequestCounter() {
		return totalRequestCounter;
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
