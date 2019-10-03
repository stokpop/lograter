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
package nl.stokpop.lograter.analysis;

import net.jcip.annotations.Immutable;

import java.util.Map;

@Immutable
public class HistogramData {
	
	private final DoubleMatrix data;
	private final long timerange;
	private final long min;
	private final long max;
	
	public HistogramData(Map<Long, Long> values, long timerange, long min, long max) {
		this.data = new DoubleMatrix(values);
		this.timerange = timerange;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public String toString() {
		return "HistogramData [timerange=" + timerange
				+ ", min=" + min + ", max=" + max + ", data=" + data + "]" ;
	}

	public long getTimeRangeInMillis() {
		return timerange;
	}
	public long getMin() {
		return min;
	}
	public long getMax() {
		return max;
	}
	public double[] getXvalues() {
		return data.getxValues();
	}
	public double[] getYvalues() {
		return data.getyValues();
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
