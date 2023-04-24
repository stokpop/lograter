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
package nl.stokpop.lograter.util.time;

import java.util.Comparator;

public class TPSMeasurement {
	
	private final float tps;
	private final long timestamp;

	public TPSMeasurement(long timestamp, float avgHitsPerDuration) {
		super();
		this.tps = avgHitsPerDuration;
		this.timestamp = timestamp;
	}

	public float getTps() {
		return tps;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	private static class SortOnTimestamp implements Comparator<TPSMeasurement> {
		
		private static final SortOnTimestamp instance = new SortOnTimestamp();
		
		private SortOnTimestamp() {}

		public static Comparator<TPSMeasurement> instance() {
			return instance;
		}

		public int compare(TPSMeasurement o1, TPSMeasurement o2) {
			long x = o1.getTimestamp();
			long y = o2.getTimestamp();
			return Long.compare(x, y);
		}
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
        return "TPSMeasurement{" + "tps=" + tps +
                ", timestamp=" + timestamp +
                '}';
	}
}