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
package nl.stokpop.lograter.util.time;

import nl.stokpop.lograter.store.TimeMeasurement;

import java.util.List;

public class TimeMeasurementsCalculator {

	public static double sd(List<TimeMeasurement> a) {
		double variance = variance(a);
		return Math.sqrt( variance ); 
	}

	public static double variance(List<TimeMeasurement> a) {
		if (a.size() == 0) return 0;
		if (a.size() == 1) return 0;
		double sum = 0;
		double mean = mean(a);
		for (TimeMeasurement i : a) sum += Math.pow((i.getDurationInMillis() - mean), 2);
		return sum / a.size() ;
	}

	public static double mean(List<TimeMeasurement> a) {
		double sum = sum(a); 
		return sum / a.size();
	}

	@SuppressWarnings({ "static-method" })
	public static double sum(List<TimeMeasurement> a) {
		if (a.size() > 0) {
			double sum = 0;
			for (TimeMeasurement i : a) {
				sum += i.getDurationInMillis();
			}
			return sum;
		}
		return 0;
	}
}
