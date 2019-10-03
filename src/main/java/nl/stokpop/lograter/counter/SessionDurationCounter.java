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
package nl.stokpop.lograter.counter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionDurationCounter extends Counter {

	private long hits = 0L;
	private List<Long> hitList = new ArrayList<>();
	
	private SessionDurationCounter(String name) {
		super(name);
	}
	
	private void setStartTimestamp(long startTimestamp) {
		this.hitList.add(startTimestamp);
	}
	
	public static SessionDurationCounter getInstance(String name, long startTimestamp) {
		SessionDurationCounter counter = new SessionDurationCounter(name);
		counter.setStartTimestamp(startTimestamp);
		return counter;
	}
	
	public void registerHit(long timestamp) {
		hits++;
		hitList.add(timestamp);
	}

	public long getHits() {
		return hits;
	}

	public List<Integer> getDurations(long timeoutInMillis) {
		
		List<Integer> durations = new ArrayList<>();
		
		Collections.sort(hitList);
	
		int length = hitList.size();
		long myStart = hitList.get(0);
		long prevHit = myStart;
		long waitTime;
		
		for (int i = 1; i < length; i++) {
			long now = hitList.get(i);
			waitTime = now - prevHit;
			if (waitTime > timeoutInMillis) {
				// timeout exceeded: end session
				durations.add((int) (prevHit - myStart));
				myStart = now;
			}
			prevHit = now;
		}
		// final duration
		int myDuration = (int) (prevHit - myStart);
		durations.add(myDuration);

		return durations;
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
