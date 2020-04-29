/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.BaseUnit;
import nl.stokpop.lograter.parser.line.DateLogEntryMapper;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackDirective;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.StringEntryMapper;
import nl.stokpop.lograter.util.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.stokpop.lograter.command.BaseUnit.milliseconds;
import static nl.stokpop.lograter.logentry.AccessLogEntry.parseRequest;

public class ApacheLogMapperFactory {

    private static final Logger log = LoggerFactory.getLogger(ApacheLogMapperFactory.class);
    public static final String DEFAULT_DATE_TIME_LOG_PATTERN = "[dd/MMM/yyyy:HH:mm:ss Z]";

    private ApacheLogMapperFactory() {}

    public static String determineDateTimePattern(List<LogbackElement> elements) {
        String dateTimePattern = null ;
        for (LogbackElement element : elements) {
            if (element instanceof LogbackDirective) {
                LogbackDirective directive = (LogbackDirective) element;
                if ("t".equals(directive.getDirective())) {
                    String variable = directive.getVariable();
                    if (variable != null) {
                        dateTimePattern = DateUtils.convertStrfTimePatternToDateTimeFormatterPattern(variable);
                    }
                }
            }
        }
        if (dateTimePattern == null) {
            dateTimePattern = DEFAULT_DATE_TIME_LOG_PATTERN;
        }
        return dateTimePattern;
    }

    public static Map<String, LogEntryMapper<AccessLogEntry>> initializeMappers(List<LogbackElement> elements) {
        return initializeMappers(elements, AccessLogEntry.URL_SPLITTER_DEFAULT);
    }

    public static Map<String, LogEntryMapper<AccessLogEntry>> initializeMappers(List<LogbackElement> elements, final UrlSplitter urlSplitter) {
        return initializeMappers(elements, urlSplitter, milliseconds);
    }

    public static Map<String, LogEntryMapper<AccessLogEntry>> initializeMappers(List<LogbackElement> elements, final UrlSplitter urlSplitter, final BaseUnit baseUnit) {
		
		Map<String, LogEntryMapper<AccessLogEntry>> mappers = new HashMap<>();
		
        String dateTimePattern = determineDateTimePattern(elements);

        // either t or msec_frac or usec_frac can be called first, add up both for epoch timestamp
		mappers.put("t",
			new DateLogEntryMapper<AccessLogEntry>(dateTimePattern) {
				public void writeToLogEntry(String value, String variable, AccessLogEntry e) {
                    if (e.getTimestamp() > 999) {
                        throw new LogRaterException("Did not expect more that 999 milliseconds to be present from msec_frac of usec_frac. Now: " + e.getTimestamp());
                    }
					e.setTimestamp(e.getTimestamp() + dateParser(value));
				}
			}
		);
		mappers.put("msec_frac",
			new DateLogEntryMapper<AccessLogEntry>(dateTimePattern) {
				public void writeToLogEntry(String value, String variable, AccessLogEntry e) {
					e.setTimestamp(e.getTimestamp() + Long.parseLong(value));
				}
			}
		);
		mappers.put("usec_frac",
			new DateLogEntryMapper<AccessLogEntry>(dateTimePattern) {
				public void writeToLogEntry(String value, String variable, AccessLogEntry e) {
                    long millis = Long.parseLong(value) / 1000;
					e.setTimestamp(e.getTimestamp() + millis);
				}
			}
		);

		mappers.put("r",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> parseRequest(value, e, urlSplitter));

		mappers.put("s",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setHttpStatus(Integer.parseInt(value)));

		mappers.put("b",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> {
                    int bytes = "-".equals(value) ? 0 : Integer.parseInt(value);
                    e.setBytes(bytes);
                });

		mappers.put("D",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> {
                    long durationInMicros = Long.parseLong(value);
                    insertDurationIntoLogEntry(e, durationInMicros, baseUnit);
                });

        mappers.put("T",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> {
                    double durationInSeconds = Double.parseDouble(value);
                    // losing nano precision here!
                    long durationInMicros = (long)(durationInSeconds * 1_000_000);
                    insertDurationIntoLogEntry(e, durationInMicros, baseUnit);
                });

        mappers.put("x",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> {
                    if ("duration-nanoseconds".equals(variable)) {
                        long durationInNanos = Long.parseLong(value);
                        long durationInMicros = Math.round(durationInNanos / 1_000.0);
                        insertDurationIntoLogEntry(e, durationInMicros, baseUnit);
                    }
		        });

		mappers.put("i",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> {
                    if ("Referer".equals(variable)) {
                        e.setReferrer(value);
                    }
                    else if ("User-Agent".equals(variable)) {
                        e.setUserAgent(value);
                    }
                    else {
                        log.debug("No special setter for i directive variable: {} with value: {}", variable, value);
                    }
                });

		mappers.put("h",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setRemoteHost(value));

		mappers.put("l",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setRemoteLogname(value));

		mappers.put("u",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setRemoteUser(value));

		return mappers;
	}

    private static void insertDurationIntoLogEntry(AccessLogEntry e, long durationInMicros, BaseUnit baseUnit) {
        e.setDurationInMicros(durationInMicros);

        long durationInMillis = durationInMicros / 1000;
        if (durationInMillis > Integer.MAX_VALUE) {
            throw new LogRaterException("Duration in millis is too large to fit in integer: " + durationInMillis);
        }
        // TODO this is now experiment with microseconds
        e.setDurationInMillis((int) (baseUnit == milliseconds ? durationInMillis : durationInMicros));
    }

    @Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}