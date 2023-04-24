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
package nl.stokpop.lograter.logentry;

import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogbackLogEntry extends LogEntry {

	private String logLevel;
	private String message;
	private String threadName;
	private String marker;
	private String logger;
	private String className;
	private final List<String> nonLogLines = new ArrayList<>();
    private final List<String> customFields = new ArrayList<>();

	@Override
	public String toString() {
        String sb = "LogbackLogEntry{" + "logLevel='" + logLevel + '\'' +
                ", message='" + message + '\'' +
                ", threadName='" + threadName + '\'' +
                ", marker='" + marker + '\'' +
                ", logger='" + logger + '\'' +
                ", className='" + className + '\'' +
                ", nonLogLines.size=" + nonLogLines.size() +
                '}';
        return sb;
	}

	public static Map<String, LogEntryMapper<LogbackLogEntry>> initializeLogBackMappers(List<LogbackElement> elements) {
		Map<String, LogEntryMapper<LogbackLogEntry>> mappers = new HashMap<>();

		LogbackMappers.initializeMappers(elements, mappers);

        return mappers;
	}

	public final String getLogLevel() {
		return logLevel;
	}

	public final String getMessage() {
		return message;
	}

	public final String getThreadName() {
		return threadName;
	}

	public final String getMarker() {
		return marker;
	}

	public final String getClassName() {
		return className;
	}

	final void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	final void setMessage(String message) {
		this.message = message;
	}

	final void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	final void setMarker(String marker) {
		this.marker = marker;
	}

	final void setClassName(String className) {
		this.className = className;
	}
	
    final void addNonLogLine(String nonLogLine) {
		this.nonLogLines.add(nonLogLine);
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public String getLogger() {
		return logger;
	}

	public final String[] getNonLogLines() {
		return nonLogLines.toArray(new String[0]);
	}

	public final int getNrOfNonLoglines() {
		return nonLogLines.size();
	}
	
	public final long getNonLogLinesLength() {
		long size = 0;
		for (String line : nonLogLines) {
			size += line.length();
		}
		return size;
	}

	public final void addNonLogLinesCopy(List<String> nonLogLinesToAdd) {
		nonLogLines.addAll(nonLogLinesToAdd);
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

    public void addCustomField(String directive, String value) {
        customFields.add(directive);
        addField(directive, value);
    }

    public String[] getCustomFields() {
        return customFields.toArray(new String[0]);
    }

}
