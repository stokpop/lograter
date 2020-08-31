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
package nl.stokpop.lograter.parser.line;

import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.logentry.LogEntry;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DateLogEntryMapper<T extends LogEntry> implements LogEntryMapper<T> {
	
	private final Logger log = LoggerFactory.getLogger(DateLogEntryMapper.class);
	
	private final DateTimeFormatter dateFormatter;
	
	public DateLogEntryMapper(String dateFormat) {
		DateTimeFormatter tempDateTimeFormatter;
		try {
			if ("ISO8601".equalsIgnoreCase(dateFormat)) {
				tempDateTimeFormatter = ISODateTimeFormat.dateTime();
			} else {
				tempDateTimeFormatter = DateTimeFormat.forPattern(dateFormat).withLocale(LogRater.DEFAULT_LOCALE);
			}
		} catch(IllegalArgumentException e) {
			if (dateFormat.contains(",")) {
				int idx = dateFormat.indexOf(',');
				String timezone = dateFormat.substring(idx);
				dateFormat = dateFormat.substring(0, idx);			
				log.warn("No support for timezones, skipping '{}', using '{}'", timezone, dateFormat);
			}
			tempDateTimeFormatter = DateTimeFormat.forPattern(dateFormat).withLocale(LogRater.DEFAULT_LOCALE);
		}
		dateFormatter = tempDateTimeFormatter;
	}
	
	protected long dateParser(String value) {
        try {
            return dateFormatter.parseDateTime(value).getMillis();
        } catch (IllegalArgumentException e) {
            throw new LogRaterException("Could not parse date.", e);
        }
    }

}
