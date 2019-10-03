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
 * TODO: make sure adding/changing the inner counter stores is impossible to retain consistency.
 * E.g. make methods to make all needed functionality available without exposing mutable internals.
 */
@NotThreadSafe
public class RequestCounterStorePair {
	private final RequestCounterStore requestCounterStoreSuccess;
	private final RequestCounterStore requestCounterStoreFailure;

	public RequestCounterStorePair(RequestCounterStore requestCounterStoreSuccess, RequestCounterStore requestCounterStoreFailure) {
		this.requestCounterStoreSuccess = requestCounterStoreSuccess;
		this.requestCounterStoreFailure = requestCounterStoreFailure;
	}

	/**
	 * Warning: do not add new counters to this success store, use addSuccess instead.
	 */
	public RequestCounterStore getRequestCounterStoreSuccess() {
		return requestCounterStoreSuccess;
	}

	/**
	 * Warning: do not add new counters to this failure store, use addFailure instead.
	 */
	public RequestCounterStore getRequestCounterStoreFailure() {
		return requestCounterStoreFailure;
	}

	public void addSuccess(String counterKey, long timestamp, int durationInMillis) {
		requestCounterStoreSuccess.add(counterKey, timestamp, durationInMillis);
		if (!requestCounterStoreFailure.contains(counterKey)) {
			requestCounterStoreFailure.addEmptyRequestCounterIfNotExists(counterKey);
		}
	}

	public void addFailure(String counterKey, long timestamp, int durationInMillis) {
		requestCounterStoreFailure.add(counterKey, timestamp, durationInMillis);
		if (!requestCounterStoreSuccess.contains(counterKey)) {
			requestCounterStoreSuccess.addEmptyRequestCounterIfNotExists(counterKey);
		}
	}

    /**
     * @return the total period covering the success and the failures.
     */
	public TimePeriod totalTimePeriod() {
	    return TimePeriod.createMaxTimePeriod(requestCounterStoreSuccess.getTotalRequestCounter().getTimePeriod(), requestCounterStoreFailure.getTotalRequestCounter().getTimePeriod());
    }

	@Override
	public String toString() {
        String sb = "RequestCounterStorePair{requestCounterStoreSuccess=" + requestCounterStoreSuccess +
                ", requestCounterStoreFailure=" + requestCounterStoreFailure + '}';
        return sb;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RequestCounterStorePair storePair = (RequestCounterStorePair) o;

		if (!Objects.equals(requestCounterStoreSuccess, storePair.requestCounterStoreSuccess))
			return false;
		return Objects.equals(requestCounterStoreFailure, storePair.requestCounterStoreFailure);
	}

	@Override
	public int hashCode() {
		int result = requestCounterStoreSuccess != null ? requestCounterStoreSuccess.hashCode() : 0;
		result = 31 * result + (requestCounterStoreFailure != null ? requestCounterStoreFailure.hashCode() : 0);
		return result;
	}

	public RequestCounterPair getTotalRequestCounterPair() {
        RequestCounter totalRequestCounterSuccess = requestCounterStoreSuccess.getTotalRequestCounter();
        return new RequestCounterPair(totalRequestCounterSuccess, requestCounterStoreFailure.getTotalRequestCounter());
	}

	public boolean isEmpty() {
		return requestCounterStoreFailure.isEmpty() && requestCounterStoreSuccess.isEmpty();
	}
}
