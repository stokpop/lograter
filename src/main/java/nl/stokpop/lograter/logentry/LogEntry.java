/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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

import java.util.HashMap;
import java.util.Map;

public class LogEntry {

	public static final String HTTP_STATUS = "httpStatus";
	public static final String HTTP_METHOD = "httpMethod";

	// timestamp in millis since epoch
	private long timestamp = 0L;
	private String logline;
	private String logFilename;
	private final Map<String, String> fieldsWithValue = new HashMap<>();
	
	public String getLogFilename() {
		return logFilename;
	}

	public void setLogFilename(String logFilename) {
		this.logFilename = logFilename;
	}

	public String getLogline() {
		return logline;
	}

	public void setLogline(String logline) {
		this.logline = logline;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public final void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public final void addField(String name, String value) {
		fieldsWithValue.put(name, value);
	}

	public final String getField(String name) {
		return fieldsWithValue.get(name);
	}

    public final String[] getFields() {
        return fieldsWithValue.keySet().toArray(new String[0]);
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
        return "LogEntry{" +
                "fieldsWithValue=" + fieldsWithValue +
                ", logFilename='" + logFilename + '\'' +
                ", logline='" + logline + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}