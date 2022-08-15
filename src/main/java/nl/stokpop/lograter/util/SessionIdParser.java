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
package nl.stokpop.lograter.util;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.logentry.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse the SessionId from a field in a log line.
 */
public class SessionIdParser {

	private static final Logger log = LoggerFactory.getLogger(SessionIdParser.class);

	public static final SessionIdParser NO_SESSION_ID_PARSER = new SessionIdParser(null, null);

	private final String sessionField;
	private final Pattern sessionFieldRegexp;

	public SessionIdParser(String sessionField, String sessionFieldRegexp) {
		if (LogRaterUtils.isEmpty(sessionField) && LogRaterUtils.isEmpty(sessionFieldRegexp)) {
			this.sessionField = null;
			this.sessionFieldRegexp = null;
		}
		else if (!LogRaterUtils.isEmpty(sessionField) && LogRaterUtils.isEmpty(sessionFieldRegexp)) {
			this.sessionField = sessionField;
			this.sessionFieldRegexp = null;
		}
		else if (!LogRaterUtils.isEmpty(sessionFieldRegexp) && !containsCapturingGroup(sessionFieldRegexp)) {
			log.warn("sessionFieldRegexp [{}] does not contain a capturing group, sessionId cannot be determined.", sessionFieldRegexp);
			this.sessionField = null;
			this.sessionFieldRegexp = null;

		}
		else {
			this.sessionField = sessionField;
			this.sessionFieldRegexp = Pattern.compile(sessionFieldRegexp);
		}
	}

	private static boolean containsCapturingGroup(String sessionFieldRegexp) {
		return sessionFieldRegexp.contains("(") && sessionFieldRegexp.contains(")");
	}

	public String parseSessionId(LogEntry entry) {
		String sessionId = null;
		// check if sessionId needs to be extracted
		if (!LogRaterUtils.isEmpty(sessionField)) {
			String sessionIdFromEntry = entry.getField(sessionField);
			if (sessionIdFromEntry == null) {
			    throw new LogRaterException(String.format("sessionField '%s' not found in entry: %s", sessionField, entry));
            }
			if (sessionFieldRegexp != null) {
				Matcher sessionIdMatcher = sessionFieldRegexp.matcher(sessionIdFromEntry);
				if (sessionIdMatcher.find()) {
					sessionId = sessionIdMatcher.group(1);
				} else {
					log.warn("No match found with session regexp [{}] for sessionId in field [{}] with value: [{}] for log entry [{}]", sessionFieldRegexp, sessionField, sessionIdFromEntry, entry);
					sessionId = sessionIdFromEntry;
				}
			} else {
				sessionId = sessionIdFromEntry;
			}
		}
		return sessionId;
	}
}
