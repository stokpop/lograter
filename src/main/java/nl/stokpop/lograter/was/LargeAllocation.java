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
package nl.stokpop.lograter.was;

import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;

/**
 * Holds data for large allocations found in native std error log files.
 */
@Immutable
public class LargeAllocation {

    private final int bytes;
    private final String type;
    private final DateTime timestamp;
    private final String threadName;
    private final String threadId;
    private final String threadStatus;
    private final String stackTrace;
	// optimization to get only first line of stack trace
	private final String firstStackTraceLine;

	public LargeAllocation(int bytes, String type, DateTime timestamp, String threadName, String threadId, String threadStatus, String stackTrace) {
		this.bytes = bytes;
		this.type = type;
		this.timestamp = timestamp;
		this.threadName = threadName;
		this.threadId = threadId;
		this.threadStatus = threadStatus;
		// large stack traces occupy a lot of memory,
		// use intern to have unique stack trace strings point to the same internal string object
		this.stackTrace = stackTrace == null ? null : stackTrace.intern();
		this.firstStackTraceLine = stackTrace == null ? null : stackTrace.split("\n")[0].intern();
	}

	public int getBytes() {
        return bytes;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "LargeAllocation{" +
                "bytes=" + bytes +
                ", stackTrace (first line) ='" + getStackTraceFirstLine() + '\'' +
                ", threadId='" + threadId + '\'' +
                ", threadName='" + threadName + '\'' +
                ", threadStatus='" + threadStatus + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                '}';
    }

    public String getStackTraceFirstLine() {
        return firstStackTraceLine;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getThreadStatus() {
        return threadStatus;
    }

    public String getStackTrace() {
        return stackTrace;
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
