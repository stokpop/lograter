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
import nl.stokpop.lograter.counter.HttpMethod;
import nl.stokpop.lograter.parser.line.ApacheLogFormatParser;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackDirective;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.LogbackLiteral;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApacheLogMapperFactoryTest {

    @Test
    public void testHttpMethodNoneOrUnknown() {
        List<LogbackElement> elements = new ArrayList<>();
        elements.add(new LogbackLiteral(";\""));
        elements.add(LogbackDirective.from("r"));
        elements.add(new LogbackLiteral("\";"));
        Map<String, LogEntryMapper<AccessLogEntry>> mapperMap = ApacheLogMapperFactory.initializeMappers(elements, null);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mapperMap, AccessLogEntry.class);

        AccessLogEntry logEntryNone = parser.parseLogLine(";\"- /url?version=1.0 2.0\";");
        assertEquals(HttpMethod.NONE, logEntryNone.getHttpMethod());
        assertEquals("2.0", logEntryNone.getVersion());
        assertEquals("/url?version=1.0", logEntryNone.getUrl());

        AccessLogEntry logEntryUnknown = parser.parseLogLine(";\"xyz /url 2.0\";");
        assertEquals(HttpMethod.UNKNOWN, logEntryUnknown.getHttpMethod());
        assertEquals("2.0", logEntryUnknown.getVersion());
        assertEquals("/url", logEntryUnknown.getUrl());

        AccessLogEntry logEntryNoneWithoutHttpVersion = parser.parseLogLine(";\" /error.do?nr=404\";");
        assertEquals(HttpMethod.UNKNOWN, logEntryNoneWithoutHttpVersion.getHttpMethod());
        assertEquals("Unknown", logEntryNoneWithoutHttpVersion.getVersion());
        assertEquals("/error.do?nr=404", logEntryNoneWithoutHttpVersion.getUrl());

        AccessLogEntry logEntryEmpty = parser.parseLogLine(";\"\";");
        assertEquals(HttpMethod.UNKNOWN, logEntryEmpty.getHttpMethod());
        assertEquals("Unknown", logEntryEmpty.getVersion());
        assertEquals("", logEntryEmpty.getUrl());

        AccessLogEntry logEntryWeirdString = parser.parseLogLine(";\"dskjfhfaskld kfljsdhfalk aksjfdhaslk alksfdjha fs\";");
        assertEquals(HttpMethod.UNKNOWN, logEntryWeirdString.getHttpMethod());
        assertEquals("Unknown", logEntryWeirdString.getVersion());
        assertEquals("dskjfhfaskld kfljsdhfalk aksjfdhaslk alksfdjha fs", logEntryWeirdString.getUrl());

    }

    @Test
    public void testVeryLongDuration() {

        List<LogbackElement> elements = new ArrayList<>();
        elements.add(new LogbackLiteral(";"));
        elements.add(LogbackDirective.from("D"));
        elements.add(new LogbackLiteral(";"));
        Map<String, LogEntryMapper<AccessLogEntry>> mapperMap = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mapperMap, AccessLogEntry.class);

        AccessLogEntry entryOneMirco = parser.parseLogLine(";1;");
        assertEquals(0, entryOneMirco.getDurationInMillis());

        AccessLogEntry entryOneThousandMircos = parser.parseLogLine(";1000;");
        assertEquals(1, entryOneThousandMircos.getDurationInMillis());

        long maxValue = Integer.MAX_VALUE;
        AccessLogEntry entryOneMaxMicros = parser.parseLogLine(";" + maxValue * 1000 + ";");
        assertEquals(maxValue, entryOneMaxMicros.getDurationInMillis());

        boolean exceptionThrown = false;
        try {
            parser.parseLogLine(";" + (maxValue * 1000) + 1 + ";");
        }
        catch (LogRaterException e) {
            exceptionThrown = true;
        }
        assertTrue("Expected an exception because duration in microseconds is too large", exceptionThrown);

    }

}