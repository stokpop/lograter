/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Immutable
public class DoubleMatrix {
	private final double[] xValues;
	private final double[] yValues;

	public DoubleMatrix(Map<Long, Long> data) {
		int size = data.size();
		xValues = new double[size];
		yValues = new double[size];
		
		List<Long> xs = new ArrayList<>(data.keySet());
		Collections.sort(xs);
		for (int i = 0; i < size; i++) {
			Long x = xs.get(i);
			xValues[i] = x.doubleValue();
			yValues[i] = data.get(x).doubleValue();
		}
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder(200);
		int maxCount = xValues.length - 1;
		for (int i = 0; i < xValues.length; i++) {
			report.append("(").append(xValues[i]).append(", ").append(yValues[i]).append(")");
            if (i != maxCount) {
				report.append(" ");
			}
		}
		return "DoubleMatrix [" + report.toString() + "]";
	}

	public double[] getxValues() {
		return xValues;
	}

	public double[] getyValues() {
		return yValues;
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