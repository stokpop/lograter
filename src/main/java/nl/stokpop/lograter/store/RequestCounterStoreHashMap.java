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
package nl.stokpop.lograter.store;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@NotThreadSafe
public class RequestCounterStoreHashMap implements RequestCounterStore {

    private final Map<CounterKey, RequestCounter> counters = new HashMap<>();
	private final String name;
	private final TimePeriod timePeriod;
    private RequestCounter totalRequestCounter;

	RequestCounterStoreHashMap(String storeName, CounterKey totalRequestName, TimePeriod timePeriod) {
		this.name = storeName;
		this.totalRequestCounter = new RequestCounter(totalRequestName, new TimeMeasurementStoreInMemory());
		this.timePeriod = timePeriod;
	}

	RequestCounterStoreHashMap(String storeName, CounterKey totalRequestsName) {
		this(storeName, totalRequestsName, TimePeriod.MAX_TIME_PERIOD);
	}

    public void add(CounterKey counterKey, long logTimestamp, int durationMillis) {
        RequestCounter requestCounter = addEmptyCounterIfNotExists(counterKey);
        requestCounter.incRequests(logTimestamp, durationMillis);
        totalRequestCounter.incRequests(logTimestamp, durationMillis);
    }

    @Override
	public String toString() {
		return "RequestCounterStoreHashMap{" +
				"name='" + name + '\'' +
				", timePeriod=" + timePeriod +
				", totalCounterHits=" + totalRequestCounter.getHits() +
				'}';
	}

	public @NotNull Iterator<RequestCounter> iterator() {
		List<RequestCounter> values = new ArrayList<>(counters.values());
		Collections.sort(values);
		return values.iterator();
	}
	
	@Override
	public boolean isEmpty() {
		return counters.isEmpty();
	}

	@Override
	public RequestCounter addEmptyCounterIfNotExists(CounterKey key) {
		if (!counters.containsKey(key)) {
			RequestCounter counter = new RequestCounter(key, new TimeMeasurementStoreInMemory());
			counters.put(key, counter);
			return counter;
		}
		else {
			return counters.get(key);
		}
	}

	@Override
	public RequestCounter get(CounterKey key) {
		return counters.get(key);
	}

	@Override
	public boolean contains(CounterKey key) {
		return counters.containsKey(key);
	}

	@Override
	public String getName() {
		return name;
	}

    @Override
    public Set<CounterKey> getCounterKeys() {
        return Collections.unmodifiableSet(counters.keySet());
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
