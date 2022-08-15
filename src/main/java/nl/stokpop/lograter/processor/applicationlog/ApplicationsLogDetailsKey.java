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
package nl.stokpop.lograter.processor.applicationlog;

import net.jcip.annotations.Immutable;
import nl.stokpop.lograter.LogRaterException;

/**
 * Key for Application log details (message plus stacktrace or other data).
 */
@Immutable
public class ApplicationsLogDetailsKey implements Comparable<ApplicationsLogDetailsKey> {

	private final String fullLoggerName;
	private final String logLevel;

    /**
     * Key for ApplicationLogDetails.
     * @param fullLoggerName the name of the logger, cannot be null
     * @param logLevel INFO, WARN, ERROR, ... cannot be null
     */
	public ApplicationsLogDetailsKey(String fullLoggerName, String logLevel) {
		if (fullLoggerName == null) {
            throw new LogRaterException("FullLoggerName cannot be null");
        }
		if (logLevel == null) {
            throw new LogRaterException("LogLevel cannot be null");
        }
		this.fullLoggerName = fullLoggerName;
		this.logLevel = logLevel;
	}

	public String getFullLoggerName() {
		return fullLoggerName;
	}

	public String getLogLevel() {
		return logLevel;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ApplicationsLogDetailsKey{");
		sb.append("fullLoggerName='").append(fullLoggerName).append('\'');
		sb.append(", logLevel='").append(logLevel).append('\'');
		sb.append('}');
		return sb.toString();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationsLogDetailsKey that = (ApplicationsLogDetailsKey) o;

        if (!fullLoggerName.equals(that.fullLoggerName)) return false;
        return logLevel.equals(that.logLevel);
    }

    @Override
    public int hashCode() {
        int result = fullLoggerName.hashCode();
        result = 31 * result + logLevel.hashCode();
        return result;
    }

    @Override
	public int compareTo(ApplicationsLogDetailsKey o) {
		if (logLevel.equals(o.logLevel)) {
			return fullLoggerName.compareTo(o.fullLoggerName);
		}
		else {
			return logLevel.compareTo(o.logLevel);
		}
	}
	
}
