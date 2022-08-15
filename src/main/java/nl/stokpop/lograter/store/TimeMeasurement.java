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

import net.jcip.annotations.Immutable;
import nl.stokpop.lograter.LogRaterException;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

/**
 * Stores time measurement data. It is supposed to be Immutable. But because it also implements
 * Externalizable, a default constructor is needed. 
 */
@Immutable
public class TimeMeasurement implements Externalizable {
	
	public static final TimeMeasurement END_OF_TIME = new TimeMeasurement(Long.MAX_VALUE, 0);

	private int durationInMillis;
	private long timestamp;
	private int numberOfHits;

	public static final Comparator<TimeMeasurement> ORDER_TIMESTAMP = SortOnTimestamp.instance();

	/* Default constructor needed for serialization via Externalizable */
	public TimeMeasurement() {}

	public TimeMeasurement(long timestamp, int durationInMillis) {
		this(timestamp, durationInMillis, 1);
	}

	/**
	 * Note that the max number of milliseconds is Integer.MAX_VALUE or 2147483647 milliseconds.
	 * That is max duration of around 596 hours.
	 *
	 * @param timestamp in millis, should be non-negative
	 * @param durationInMillis should be non-negative
	 * @param numberOfHits the number of hits for this timestamp (default is 1, used for reduced counters)
	 * @throws LogRaterException when timestamp or duration is negative
	 */
	public TimeMeasurement(long timestamp, int durationInMillis, int numberOfHits) {
		super();
        if (timestamp < 0) {
            throw new LogRaterException("Timestamp is not allowed to be negative: " + timestamp);
        }
        if (durationInMillis < 0) {
            throw new LogRaterException("Duration is not allowed to be negative: " + durationInMillis);
        }
		if (numberOfHits < 0) {
			throw new LogRaterException("Number of hits is not allowed to be negative: " + numberOfHits);
		}
		this.durationInMillis = durationInMillis;
		this.timestamp = timestamp;
		this.numberOfHits = numberOfHits;
	}

	public int getDurationInMillis() {
		return durationInMillis;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getNumberOfHits() {
		return numberOfHits;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(timestamp);
		out.writeInt(durationInMillis);
		out.write(numberOfHits);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException {
		this.timestamp = in.readLong();
		this.durationInMillis = in.readInt();
		this.numberOfHits = in.readInt();
	}

	private static class SortOnTimestamp implements Comparator<TimeMeasurement> {
		
		private static final SortOnTimestamp instance = new SortOnTimestamp();
		
		private SortOnTimestamp() {}

		public static Comparator<TimeMeasurement> instance() {
			return instance;
		}

		public int compare(TimeMeasurement o1, TimeMeasurement o2) {
			long x = o1.getTimestamp();
			long y = o2.getTimestamp();
			return Long.compare(x, y);
		}
	}

	@Override
	public String toString() {
        return "TimeMeasurement{" + "durationInMillis=" + durationInMillis +
                ", timestamp=" + timestamp +
                ", numberOfHits=" + numberOfHits +
                '}';
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}
	
}
