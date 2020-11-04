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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.command.LatencyUnit;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;

public class LatencyLogConfig extends BasicCounterLogConfig {
	
	private String counterFields;
	private String logPattern;
	private boolean determineClickpaths;
	private String mapperFile;
	private String sessionField;
	private int clickPathShortCodeLength;
	private String latencyField;
	private LatencyUnit latencyUnit;

	public String getCounterFields() {
		return counterFields;
	}

	public void setCounterFields(String counterFields) {
		this.counterFields = counterFields;
	}

	public String getLogPattern() {
		return logPattern;
	}

	public void setLogPattern(String logPattern) {
		this.logPattern = logPattern;
	}

	public boolean isDetermineClickpaths() {
		return determineClickpaths;
	}

	public void setDetermineClickpaths(boolean determineClickpaths) {
		this.determineClickpaths = determineClickpaths;
	}

	public String getMapperFile() {
		return mapperFile;
	}

	public void setMapperFile(String mapperFile) {
		this.mapperFile = mapperFile;
	}

	public String getSessionField() {
		return sessionField;
	}

	public void setSessionField(String sessionField) {
		this.sessionField = sessionField;
	}

	public int getClickPathShortCodeLength() {
		return clickPathShortCodeLength;
	}

	public void setClickPathShortCodeLength(int clickPathShortCodeLength) {
		this.clickPathShortCodeLength = clickPathShortCodeLength;
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	public String getLatencyField() {
		return latencyField;
	}

	public void setLatencyField(String latencyField) {
		this.latencyField = latencyField;
	}

	public LatencyUnit getLatencyUnit() {
		return latencyUnit;
	}

	public void setLatencyUnit(LatencyUnit latencyUnit) {
		this.latencyUnit = latencyUnit;
	}
}
