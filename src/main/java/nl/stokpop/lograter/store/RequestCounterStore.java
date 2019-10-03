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

import nl.stokpop.lograter.counter.RequestCounter;

import java.util.List;

/**
 * A RequestCounterStore has one RequestCounter for the total of all requests.
 * And one RequestCounter for each counterKey. A counterKey can be a url for instance.
 */
public interface RequestCounterStore extends Iterable<RequestCounter> {
	String getName();
    List<String> getCounterKeys();
	RequestCounter get(String counterKey);
	RequestCounter getTotalRequestCounter();

	boolean contains(String counterKey);
	void add(String counterKey, long timestamp, int duration);
	boolean isEmpty();

	/**
	 * Add new empty RequestCounter if RequestCounter with counterKey does not exist.
	 * @param counterKey the name of the RequestCounter
	 * @return the new or existing request counter.
	 */
	RequestCounter addEmptyRequestCounterIfNotExists(String counterKey);
}
