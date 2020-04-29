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
package nl.stokpop.lograter.processor;

import nl.stokpop.lograter.util.time.DataTimePeriod;
import nl.stokpop.lograter.util.time.TimePeriod;

public abstract class BasicLogData {

	private final DataTimePeriod dataTimePeriod = new DataTimePeriod();

	private long filteredLines;
	private long nonLogLines;
	private long totalCharacters;
	private long totalLogLines;

	public BasicLogData() {	
	}

	public TimePeriod getLogTimePeriod() {
		return dataTimePeriod.getLogTimePeriod();
	}

	public void updateLogTime(long timestamp) {
		dataTimePeriod.updateDataTime(timestamp);
	}

	public long getNonLogLines() {
		return nonLogLines;
	}
	
	public void incNonLogLines(int nrOfNonLogLines) {
		nonLogLines += nrOfNonLogLines;
	}

	public long getFilteredLines() {
		return filteredLines;
	}

	public void incFilteredLines() {
		filteredLines++;
	}

	public void incLogLines() {
		totalLogLines++;
	}

	public long getTotalCharacters() {
		return totalCharacters;
	}

	public void incTotalCharacters(long nrOfCharacters) {
		this.totalCharacters += nrOfCharacters;
	}

	public long getTotalLogLines() {
		return totalLogLines + nonLogLines;
	}

	public long getTotalMB() {
		return this.totalCharacters / (1024 * 1024);
	}

	@Override
	public String toString() {
		return "BasicLogData{" +
				"dataTimePeriod=" + dataTimePeriod +
				", filteredLines=" + filteredLines +
				", nonLogLines=" + nonLogLines +
				", totalCharacters=" + totalCharacters +
				", totalLogLines=" + totalLogLines +
				'}';
	}
}