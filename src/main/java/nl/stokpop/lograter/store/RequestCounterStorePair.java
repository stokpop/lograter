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
import nl.stokpop.lograter.counter.RequestCounterPair;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.util.Objects;

/**
 * Holds a separate RequestCounterStore for success and for failure.
 * Makes sure both stores have same number of RequestCounters, possibly with empty request counters.
 * To avoid null pointer exceptions: add successes and failures via the RequestCounterStorePair only!
 *
 * E.g. make methods to make all needed functionality available without exposing mutable internals.
 */
@NotThreadSafe
public class RequestCounterStorePair {
	private final RequestCounterStore storeSuccess;
	private final RequestCounterStore storeFailure;

	public RequestCounterStorePair(RequestCounterStore storeSuccess, RequestCounterStore storeFailure) {
		this.storeSuccess = storeSuccess;
		this.storeFailure = storeFailure;
	}

	/**
	 * Warning: do not add new counters to this success store, use addSuccess instead.
     * @return read only request counter
	 */
	public RequestCounterStore getRequestCounterStoreSuccess() {
		return new RequestCounterStoreReadOnly(storeSuccess);
	}

	/**
	 * Warning: do not add new counters to this failure store, use addFailure instead.
     * @return read only request counter
     */
	public RequestCounterStore getStoreFailure() {
	    return new RequestCounterStoreReadOnly(storeFailure);
	}

	public void addSuccess(String counterKey, long timestamp, int durationInMillis) {
		storeSuccess.add(counterKey, timestamp, durationInMillis);
		if (!storeFailure.contains(counterKey)) {
			storeFailure.addEmptyRequestCounterIfNotExists(counterKey);
		}
	}

	public void addFailure(String counterKey, long timestamp, int durationInMillis) {
		storeFailure.add(counterKey, timestamp, durationInMillis);
		if (!storeSuccess.contains(counterKey)) {
			storeSuccess.addEmptyRequestCounterIfNotExists(counterKey);
		}
	}

    /**
     * @return the total period covering the success and the failures.
     */
	public TimePeriod totalTimePeriod() {
	    return TimePeriod.createMaxTimePeriod(storeSuccess.getTotalRequestCounter().getTimePeriod(), storeFailure.getTotalRequestCounter().getTimePeriod());
    }
    
	@Override
	public String toString() {
        String sb = "RequestCounterStorePair{requestCounterStoreSuccess=" + storeSuccess +
                ", requestCounterStoreFailure=" + storeFailure + '}';
        return sb;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RequestCounterStorePair storePair = (RequestCounterStorePair) o;

		if (!Objects.equals(storeSuccess, storePair.storeSuccess))
			return false;
		return Objects.equals(storeFailure, storePair.storeFailure);
	}

	@Override
	public int hashCode() {
		int result = storeSuccess != null ? storeSuccess.hashCode() : 0;
		result = 31 * result + (storeFailure != null ? storeFailure.hashCode() : 0);
		return result;
	}

	public RequestCounterPair getTotalRequestCounterPair() {
        RequestCounter totalRequestCounterSuccess = storeSuccess.getTotalRequestCounter();
        return new RequestCounterPair(totalRequestCounterSuccess, storeFailure.getTotalRequestCounter());
	}

	public boolean isEmpty() {
		return storeFailure.isEmpty() && storeSuccess.isEmpty();
	}
}
