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
package nl.stokpop.lograter.logentry;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.FailureFieldType;
import nl.stokpop.lograter.command.LatencyUnit;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.StringEntryMapper;
import nl.stokpop.lograter.processor.latency.LatencyLogConfig;
import nl.stokpop.lograter.util.HttpUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class LatencyLogEntry extends LogbackLogEntry {

	public static final LogEntrySuccessFactor<LatencyLogEntry> ALWAYS_SUCCESS = entry -> true;

	private final LogEntrySuccessFactor<LatencyLogEntry> logEntrySuccessFactor;

	private int durationInMillis;

	public LatencyLogEntry(LogEntrySuccessFactor<LatencyLogEntry> logEntrySuccessFactor) {
		this.logEntrySuccessFactor = logEntrySuccessFactor;
	}
    /**
     * The Latency Log mapper for the latency field translates the duration in milliseconds.
     * Otherwise equal to LogBackLogEntry.
     */
	public static Map<String, LogEntryMapper<LatencyLogEntry>> initializeLatencyLogMappers(List<LogbackElement> elements, LatencyLogConfig latencyConfig) {
		Map<String, LogEntryMapper<LatencyLogEntry>> mappers = new HashMap<>();

		List<String> counterFields = latencyConfig.getCounterFields();
		if (counterFields == null || counterFields.isEmpty()) {
			throw new LogRaterException("Counter fields collection is empty. Provide a field from the logback pattern to be used as counter key.");
		}
        LatencyMappers.initializeMappers(elements, mappers, counterFields.get(0));

		String latencyField = latencyConfig.getLatencyField();
		if (latencyField == null || latencyField.trim().length() == 0) {
			throw new LogRaterException("Latency field definition is empty. Provide a latency field from the logback pattern to use for duration determination.");
		}
		final Pattern latencyPattern = findLatencyPattern(elements, latencyField);

		final LatencyUnit latencyUnit = latencyConfig.getLatencyUnit();
		if (latencyUnit == null) {
			throw new LogRaterException("Latency unit is not definition. Specifies the time unit to use for the latency field.");
		}

		StringEntryMapper<LatencyLogEntry> latencyLogEntryMapper = (value, variable, e) -> {
			String durationAsString = latencyPattern == null ? value : latencyPattern.matcher(value).group(0);
			e.durationInMillis = parseLatencyToMillis(durationAsString, latencyUnit);
		};

        mappers.put(latencyField, latencyLogEntryMapper);

        return mappers;
	}

	private static int parseLatencyToMillis(String durationAsString, LatencyUnit latencyUnit) {

		switch (latencyUnit) {
			case milliseconds:
				return Integer.parseInt(durationAsString);
			case seconds: {
				double durationAsIs = Double.parseDouble(durationAsString);
				return (int) (durationAsIs * 1000);
			}
			case microseconds: {
				if (!durationAsString.isEmpty()) {
					durationAsString = durationAsString.replace(",", "");
				}
				int durationAsIs = Integer.parseInt(durationAsString);
				return durationAsIs / 1000;
			}
			case nanoseconds: {
				if (!durationAsString.isEmpty()) {
					durationAsString = durationAsString.replace(",", "");
				}
				long durationAsIs = Long.parseLong(durationAsString);
				return (int) (durationAsIs / 1_000_000);
			}
			default:
				throw new LogRaterException("Calculation for " + latencyUnit + " for " + durationAsString + " is not implemented yet.");
		}
	}

	/**
	 *  @return latency pattern if found, null if it is no regexp pattern: does not contain "(" and ")" for a regexp group.
	 */
	@Nullable
	private static Pattern findLatencyPattern(List<LogbackElement> elements, String latencyField) {
		Pattern latencyPattern;
		String latencyRegexp = LogbackElement.findVariableForElement(elements, latencyField);
		if (latencyRegexp == null) {
			latencyPattern = null;
		}
		else if (!containsRegexpGroup(latencyRegexp)) {
			throw new LogRaterException("The latency variable of " + latencyField + " field should contain a regexp with 1 group for the latency value, like (.*?)");
		}
		else {
		 	latencyPattern = Pattern.compile(latencyRegexp);
		}
		return latencyPattern;
	}

	private static boolean containsRegexpGroup(String latencyRegexp) {
		return latencyRegexp.contains("(") && latencyRegexp.contains(")");
	}

	public int getDurationInMillis() {
		return durationInMillis;
	}

	public void setDurationInMillis(int durationInMillis) {
		this.durationInMillis = durationInMillis;
	}

	public boolean isSuccess() {
		return logEntrySuccessFactor.isSuccess(this);
	}

	public static LogEntrySuccessFactor<LatencyLogEntry> successFactorInstance(String failureField, FailureFieldType type, Pattern failureFieldRegexp) {
		if (type == FailureFieldType.regexp && failureFieldRegexp == null) { throw new LogRaterException("failureFieldReqexp cannot be null when failureFieldType is regexp"); }
	    return entry -> successFactor(entry, failureField, type, failureFieldRegexp);
	}

	private static boolean successFactor(LatencyLogEntry entry, String failureField, FailureFieldType type, Pattern failureFieldRegexp) {
		String value = entry.getField(failureField);

		if (value == null) { return true; }

		switch (type) {
			case bool: return !Boolean.parseBoolean(value);
			case http: return isSuccessHttpStatusCode(value);
			case regexp: return !failureFieldRegexp.matcher(value).find();
		}

		return true;
	}

	/**
	 * @return false if http status code is an known error code, true otherwise, also when text is not a number.
	 */
	private static boolean isSuccessHttpStatusCode(String text) {
		int httpStatusCode;
		try {
			httpStatusCode = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return true;
		}
		return !HttpUtil.isHttpError(httpStatusCode);
	}

	@Override
	public String toString() {
		return "LatencyLogEntry [durationInMillis=" + durationInMillis
				+ ", toString()=" + super.toString() + "]";
	}

}
