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
package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.FailureFieldType;
import nl.stokpop.lograter.command.LatencyUnit;
import nl.stokpop.lograter.processor.accesslog.MapperAndClickPathConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LatencyLogConfig extends MapperAndClickPathConfig {
	
	private List<String> counterFields;
	private String logPattern;
	private String latencyField;
	private LatencyUnit latencyUnit;
	private String failureField;
	private FailureFieldType failureFieldType;
	private String failureFieldRegexp;

	public String getFailureField() {
		return failureField;
	}

	public void setFailureField(String failureField) {
		this.failureField = failureField;
	}

	public FailureFieldType getFailureFieldType() {
		return failureFieldType;
	}

	public void setFailureFieldType(FailureFieldType failureFieldType) {
		this.failureFieldType = failureFieldType;
	}

	public String getFailureFieldRegexp() {
		return failureFieldRegexp;
	}

	public void setFailureFieldRegexp(String failureFieldRegexp) {
		this.failureFieldRegexp = failureFieldRegexp;
	}

	private static List<String> counterFieldsToStringArray(String counterFields) {
		return Collections.unmodifiableList(Arrays.asList(counterFields.replace(" ", "").split(",")));
	}

	public List<String> getCounterFields() {
		return counterFields;
	}

	public void setCounterFields(String counterFields) {
		if (counterFields == null || counterFields.isEmpty()) throw new LogRaterException("counterFields cannot be null or empty.");
		this.counterFields = counterFieldsToStringArray(counterFields);
	}

	public String getLogPattern() {
		return logPattern;
	}

	public void setLogPattern(String logPattern) {
		this.logPattern = logPattern;
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

	@Override
	public String toString() {
		return "LatencyLogConfig{" +
			"counterFields=" + counterFields +
			", logPattern='" + logPattern + '\'' +
			", latencyField='" + latencyField + '\'' +
			", latencyUnit=" + latencyUnit +
			", failureField='" + failureField + '\'' +
			", failureFieldType=" + failureFieldType +
			", failureFieldRegexp='" + failureFieldRegexp + '\'' +
			"} " + super.toString();
	}
}
