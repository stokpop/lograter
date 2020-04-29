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
package nl.stokpop.lograter.logentry;

import nl.stokpop.lograter.parser.line.DateLogEntryMapper;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackDirective;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.StringEntryMapper;

import java.util.List;
import java.util.Map;

/**
 * Contains logic for the mappers.
 */
public class LogbackMappers {

    public static final String STANDARD_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

    public static String determineDateFormat(List<LogbackElement> elements) {
        String dateFormat = null;
        for (LogbackElement element : elements) {
            if (element instanceof LogbackDirective) {
                LogbackDirective logbackDirective = (LogbackDirective) element;
                String directive = logbackDirective.getDirective();
                if ("d".equals(directive) || "date".equals(directive)) {
                    dateFormat = logbackDirective.getVariable();
                }
            }
        }

        if (dateFormat == null) {
            dateFormat = STANDARD_TIME_FORMAT;
        }
        return dateFormat;
    }

    /**
     * Follows logback field conventions (a subset): http://logback.qos.ch/manual/layouts.html
     */
    public static <T extends LogbackLogEntry> void initializeMappers(List<LogbackElement> elements, Map<String, LogEntryMapper<T>> mappers) {

        String dateFormat = determineDateFormat(elements);

        final DateLogEntryMapper<T> dateLogEntryMapper = new DateLogEntryMapper<T>(dateFormat) {
            public void writeToLogEntry(String value, String variable, LogbackLogEntry e) {
                e.setTimestamp(dateParser(value));
            }
        };
        mappers.put("d", dateLogEntryMapper);
        mappers.put("date", dateLogEntryMapper);

        final StringEntryMapper<T> threadEntryMapper = (value, variable, e) -> e.setThreadName(value);
        mappers.put("t", threadEntryMapper);
        mappers.put("thread", threadEntryMapper);

        final StringEntryMapper<T> levelEntryMapper = (value, variable, e) -> e.setLogLevel(value);
        mappers.put("p", levelEntryMapper);
        mappers.put("le", levelEntryMapper);
        mappers.put("level", levelEntryMapper);

        mappers.put("marker", (StringEntryMapper<T>) (value, variable, e) -> e.setMarker(value));

        final StringEntryMapper<T> classEntryMapper = (value, variable, e) -> e.setClassName(value);
        mappers.put("C", classEntryMapper);
        mappers.put("class", classEntryMapper);

        final StringEntryMapper<T> messageEntryMapper = (value, variable, e) -> e.setMessage(value);
        mappers.put("m", messageEntryMapper);
        mappers.put("msg", messageEntryMapper);
        mappers.put("emsg", messageEntryMapper);
        mappers.put("message", messageEntryMapper);

        final StringEntryMapper<T> loggerEntryMapper = (value, variable, entry) -> entry.setLogger(value);
        mappers.put("c", loggerEntryMapper);
        mappers.put("lo", loggerEntryMapper);
        mappers.put("logger", loggerEntryMapper);
    }

}
