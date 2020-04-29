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
package nl.stokpop.lograter.util.time;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.SessionDurationCounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionDurationCalculator {
	
	private Map<String, SessionDurationCounter> counters = new HashMap<>();
	private final long timeoutInMillis;
	
	public SessionDurationCalculator(long sessionTimeOutInSeconds) {
		timeoutInMillis = sessionTimeOutInSeconds * 1000;
	}
	public SessionDurationCalculator() {
		this(30 * 60);
	}
	
	public void addHit(String id, long timestamp) {

		if (id == null) {
			throw new LogRaterException("Do not give null as a session id to the SessionDurationCalculator.");
		}
		
		SessionDurationCounter counter = counters.get(id);
		
		if (counter == null) {
			counter = SessionDurationCounter.getInstance(id, timestamp);
			counters.put(id, counter);
		}
		else {
			counter.registerHit(timestamp);
		}
		
	}
	
	public long getAvgSessionDuration() {
		Collection<SessionDurationCounter> sessionCounters = counters.values();
		List<Integer> sessionDurations = new ArrayList<>();
		for (SessionDurationCounter sessionCounter : sessionCounters) {
			sessionDurations.addAll(sessionCounter.getDurations(timeoutInMillis));
		}
		long total = 0L;
		for (Integer sessionDuration : sessionDurations) {
			total += sessionDuration;
		}
		if (sessionDurations.size() == 0) {
			return 0;
		}
		return total / sessionDurations.size();
	}

}
