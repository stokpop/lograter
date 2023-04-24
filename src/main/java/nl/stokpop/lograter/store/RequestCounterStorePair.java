/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterKey;
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
 *
 * If RequestCounterStoreMaxCounters types are given, both success and failure store should be of
 * same type and have the same max counters value for this store pair to work correctly.
 * A LogRaterException is thrown when this rule is violated.
 *
 */
@NotThreadSafe
public class RequestCounterStorePair {
	private final RequestCounterStore storeSuccess;
	private final RequestCounterStore storeFailure;
    private final RequestCounterStoreReadOnly readOnlyStoreSuccess;
    private final RequestCounterStoreReadOnly readOnlyStoreFailure;

    public RequestCounterStorePair(RequestCounterStore storeSuccess, RequestCounterStore storeFailure) {
        this.storeSuccess = storeSuccess;
		this.storeFailure = storeFailure;
		this.readOnlyStoreSuccess = new RequestCounterStoreReadOnly(storeSuccess);
		this.readOnlyStoreFailure = new RequestCounterStoreReadOnly(storeFailure);

        sanityCheckMaxCountersThrowsException(storeSuccess, storeFailure);

    }

    private void sanityCheckMaxCountersThrowsException(RequestCounterStore storeSuccess, RequestCounterStore storeFailure) {
        if (storeSuccess instanceof RequestCounterStoreMaxCounters) {
            if (!(storeFailure instanceof RequestCounterStoreMaxCounters)) {
                String msg = String.format("Both counter store should be of same type RequestCounterStoreMaxCounters: success: %s failure: %s", storeSuccess, storeFailure);
                throw new LogRaterException(msg);
            }
            int maxSuccess =  ((RequestCounterStoreMaxCounters)storeSuccess).getMaxUniqueCounters();
            int maxFailure =  ((RequestCounterStoreMaxCounters)storeFailure).getMaxUniqueCounters();
            if (maxSuccess != maxFailure) {
                String msg = String.format("Both RequestCounterStoreMaxCounters must have same max unique counters value. success: %d, failure: %d", maxSuccess, maxFailure);
                throw new LogRaterException(msg);
            }
        }
    }

    /**
	 * Warning: do not add new counters to this success store, use addSuccess instead.
     * @return read only request counter
	 */
	public RequestCounterStore getRequestCounterStoreSuccess() {
		return readOnlyStoreSuccess;
	}

	/**
	 * Warning: do not add new counters to this failure store, use addFailure instead.
     * @return read only request counter
     */
	public RequestCounterStore getRequestCounterStoreFailure() {
	    return readOnlyStoreFailure;
	}

	public void addSuccess(CounterKey key, long timestamp, int durationInMillis) {
		storeSuccess.add(key, timestamp, durationInMillis);
		if (!storeFailure.contains(key)) {
			storeFailure.addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(key);
			assert storeSuccess.isOverflowing() == storeFailure.isOverflowing();
		}
	}

	public void addFailure(CounterKey key, long timestamp, int durationInMillis) {
		storeFailure.add(key, timestamp, durationInMillis);
		if (!storeSuccess.contains(key)) {
			storeSuccess.addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(key);
            assert storeSuccess.isOverflowing() == storeFailure.isOverflowing();
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
        return "RequestCounterStorePair{requestCounterStoreSuccess=" + storeSuccess +
                ", requestCounterStoreFailure=" + storeFailure + '}';
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

	public boolean isOverflowing() {
	    // actually both should be overflowing at same time
		return storeFailure.isOverflowing() && storeSuccess.isOverflowing();
	}
}
