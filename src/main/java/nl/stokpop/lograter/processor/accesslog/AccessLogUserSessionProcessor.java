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
package nl.stokpop.lograter.processor.accesslog;

import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.LogRaterUtils;
import nl.stokpop.lograter.util.time.SessionDurationCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessLogUserSessionProcessor implements Processor<AccessLogEntry> {

	private final static Logger log = LoggerFactory.getLogger(AccessLogUserSessionProcessor.class);

	private SessionDurationCalculator calculator;
	private long countOfNullSessionIds = 0;

	public AccessLogUserSessionProcessor(SessionDurationCalculator calculator) {
		this.calculator = calculator;
	}

	/**
	 * Process user session data.
	 * @param entry should contain a sessionId, if sessionId is null, it will be skipped with a warning in the log file
	 */
	@Override
	public void processEntry(AccessLogEntry entry) {

		String sessionId = entry.getSessionId();

		if (sessionId == null) {
			countOfNullSessionIds++;
			if (LogRaterUtils.isMod10Count(countOfNullSessionIds)) {
				log.warn("There are {} null sessionIds provided, these are skipped in the session duration calculator!", countOfNullSessionIds);
			}
		}
		else {
			calculator.addHit(sessionId, entry.getTimestamp());
		}
	}

	public SessionDurationCalculator getSessionDurationCalculator() {
		return calculator;
	}

}
