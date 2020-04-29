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

import org.joda.time.DateTime;

public class LargeAllocationBuilder {
	private int bytes;
	private String type;
	private DateTime timestamp;
	private String threadName;
	private String threadId;
	private String threadStatus;
	private String stackTrace;

	public LargeAllocationBuilder setBytes(int bytes) {
		this.bytes = bytes;
		return this;
	}

	public LargeAllocationBuilder setType(String type) {
		this.type = type;
		return this;
	}

	public LargeAllocationBuilder setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public LargeAllocationBuilder setThreadName(String threadName) {
		this.threadName = threadName;
		return this;
	}

	public LargeAllocationBuilder setThreadId(String threadId) {
		this.threadId = threadId;
		return this;
	}

	public LargeAllocationBuilder setThreadStatus(String threadStatus) {
		this.threadStatus = threadStatus;
		return this;
	}

	public LargeAllocationBuilder setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
		return this;
	}

	public LargeAllocation createLargeAllocation() {
		return new LargeAllocation(bytes, type, timestamp, threadName, threadId, threadStatus, stackTrace);
	}
}