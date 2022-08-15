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

import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;

import java.util.Set;

/**
 * A RequestCounterStore has one RequestCounter for the total of all requests.
 * And one RequestCounter for each counterKey. A counterKey can be a url for instance.
 */
public interface RequestCounterStore extends Iterable<RequestCounter> {
	String getName();
    Set<CounterKey> getCounterKeys();
	RequestCounter get(CounterKey key);
	RequestCounter getTotalRequestCounter();

	boolean contains(CounterKey key);
    void add(CounterKey counterKey, long timestamp, int duration);
    boolean isEmpty();

    /**
     * @return false when no more unique counter keys can be added
     * and an OVERFLOW_COUNTER is present.
     */
    default boolean isOverflowing() { return false; }
    
	/**
	 * Add new empty RequestCounter if RequestCounter with counterKey does not exist.
	 * @param counterKey the name of the RequestCounter
	 * @return the new or existing request counter.
	 */
	RequestCounter addEmptyCounterIfNotExistsOrOverflowCounterWhenFull(CounterKey counterKey);
}
