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
package nl.stokpop.lograter.processor.applicationlog;

import net.jcip.annotations.Immutable;

import java.util.Objects;

/**
 * Stores a message with additional non-loglines (such as stacktraces and messages)
 */
@Immutable
public class ApplicationLogDetails implements Comparable<ApplicationLogDetails> {
	private final String message;
	private final String details;

	public ApplicationLogDetails(String message, String details) {
		this.message = message;
		this.details = details;
	}

	public ApplicationLogDetails(String message, String[] details) {
		this(message, createOneString(details));
	}

	public static String createOneString(String[] nonLogLines) {
		if (nonLogLines == null) {
			return null;
		}
		if (nonLogLines.length == 0) {
			return null;
		}
		if (nonLogLines.length == 1) {
			return nonLogLines[0];
		}
		StringBuilder messageDetailsBuilder = new StringBuilder();
		for (String line : nonLogLines) {
			messageDetailsBuilder.append(line).append("\n");
		}
		String message = messageDetailsBuilder.toString();
		// skip last newline
		return message.substring(0, message.length() - 1);
	}

	public String getMessage() {
		return message;
	}

	public String getDetails() {
		return details;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ApplicationLogDetails details1 = (ApplicationLogDetails) o;

		if (!Objects.equals(message, details1.message)) return false;
		return Objects.equals(details, details1.details);
	}

	@Override
	public int hashCode() {
		int result = message != null ? message.hashCode() : 0;
		result = 31 * result + (details != null ? details.hashCode() : 0);
		return result;
	}

	@Override
	public int compareTo(ApplicationLogDetails o) {
		if (this.message == null) {
			return o.message == null ? 0 : -1;
		}
		if (this.message.equals(o.message)) {
			if (this.details == null) {
				return -1;
			}
			return o.details == null ? 1 : this.details.compareTo(o.details);
		}
		return o.message == null ? 1 : this.message.compareTo(o.message);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ApplicationLogDetails{");
		sb.append("message='").append(message).append('\'');
		sb.append(", details='").append(details).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
