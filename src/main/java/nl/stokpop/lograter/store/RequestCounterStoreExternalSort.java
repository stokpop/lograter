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
package nl.stokpop.lograter.store;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.File;
import java.util.*;

@NotThreadSafe
public class RequestCounterStoreExternalSort implements RequestCounterStore {

	private static final int BUFFER_SIZE = 100_000;
	private final Map<CounterKey, RequestCounter> counters = new HashMap<>();
	private final String name;
	private final TimePeriod timePeriod;
	private final RequestCounter totalRequestCounter;
	private final File rootStorageDir;

	RequestCounterStoreExternalSort(File rootStorageDir, String storeName, CounterKey totalRequestKey, TimePeriod timePeriod) {
		this.name = storeName;
		this.rootStorageDir = rootStorageDir;
		this.totalRequestCounter = new RequestCounter(totalRequestKey, new TimeMeasurementStoreToFiles(this.rootStorageDir, this.name, totalRequestKey.getName(), BUFFER_SIZE));
		this.timePeriod = timePeriod;
	}
	
	public void add(CounterKey counterKey, long timestamp, int durationMillis) {
		RequestCounter counter = addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(counterKey);
		counter.incRequests(timestamp, durationMillis);
		totalRequestCounter.incRequests(timestamp, durationMillis);
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
	public RequestCounter addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(CounterKey counterKey) {
		if (!counters.containsKey(counterKey)) {
			RequestCounter counter = new RequestCounter(counterKey, new TimeMeasurementStoreToFiles(rootStorageDir, name, counterKey.getName(), BUFFER_SIZE));
			counters.put(counterKey, counter);
			return counter;
		}
		else {
			return counters.get(counterKey);
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
