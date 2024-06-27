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

import nl.stokpop.lograter.parser.line.DateLogEntryMapper;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.StringEntryMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.stokpop.lograter.logentry.AccessLogEntry.URL_SPLITTER_DEFAULT;
import static nl.stokpop.lograter.logentry.AccessLogEntry.parseRequest;

public class NginxLogMapperFactory {

    private NginxLogMapperFactory() {
    }

    public static Map<String, LogEntryMapper<AccessLogEntry>> initializeMappers(List<LogbackElement> elements) {
        return initializeMappers(elements, URL_SPLITTER_DEFAULT);
    }

    public static Map<String, LogEntryMapper<AccessLogEntry>> initializeMappers(List<LogbackElement> elements, final UrlSplitter urlSplitter) {

		Map<String, LogEntryMapper<AccessLogEntry>> mappers = new HashMap<>();

        String dateTimePattern = "dd/MMM/yyyy:HH:mm:ss Z";

		mappers.put("time_local",
			new DateLogEntryMapper<AccessLogEntry>(dateTimePattern) {
				public void writeToLogEntry(String value, String variable, AccessLogEntry e) {
					e.setTimestamp(dateParser(value));
				}
			}
		);

		mappers.put("request",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> parseRequest(value, e, urlSplitter));

		mappers.put("status",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setHttpStatus(Integer.parseInt(value)));

		mappers.put("body_bytes_sent",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> {
                    int bytes = "-".equals(value) ? 0 : Integer.parseInt(value);
                    e.setBytes(bytes);
                });

        mappers.put("http_referer",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setReferrer(value));

        mappers.put("http_user_agent",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setUserAgent(value));

		mappers.put("remote_addr",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setRemoteHost(value));

		mappers.put("remote_user",
                (StringEntryMapper<AccessLogEntry>) (value, variable, e) -> e.setRemoteUser(value));

		return mappers;
	}
}