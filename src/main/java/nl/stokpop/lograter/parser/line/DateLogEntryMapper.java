/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
	private final EpochFormat epochFormat;

	private enum EpochFormat {
		NONE,
		MILLIS,
		SECONDS
	}

	public DateLogEntryMapper(String dateFormat) {
		epochFormat = determineEpochFormat(dateFormat);
		if (epochFormat != EpochFormat.NONE) {
			dateFormatter = null;
			return;
		}

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

	private static EpochFormat determineEpochFormat(String dateFormat) {
		if (dateFormat == null) {
			return EpochFormat.NONE;
		}
		String df = dateFormat.trim().toLowerCase();
		// supported aliases
		if ("epoch".equals(df) || "epoch_millis".equals(df) || "epochmillis".equals(df) || "epochmilliseconds".equals(df)) {
			return EpochFormat.MILLIS;
		}
		if ("epoch_second".equals(df) || "epoch_seconds".equals(df) || "epochsecond".equals(df) || "epochseconds".equals(df)) {
			return EpochFormat.SECONDS;
		}
		return EpochFormat.NONE;
	}

	protected long dateParser(String value) {
        try {
			if (epochFormat == EpochFormat.MILLIS) {
				return Long.parseLong(value);
			}
			if (epochFormat == EpochFormat.SECONDS) {
				return Math.multiplyExact(Long.parseLong(value), 1000L);
			}
			return dateFormatter.parseMillis(value);
		} catch (NumberFormatException e) {
			throw new LogRaterException("Could not parse epoch date.", e);
		} catch (IllegalArgumentException e) {
			throw new LogRaterException("Could not parse date.", e);
		} catch (ArithmeticException e) {
			throw new LogRaterException("Epoch seconds value overflows millis range.", e);
        }
    }

}
