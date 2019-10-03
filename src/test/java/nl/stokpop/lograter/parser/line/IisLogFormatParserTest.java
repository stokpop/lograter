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
package nl.stokpop.lograter.parser.line;

import nl.stokpop.lograter.counter.HttpMethod;
import nl.stokpop.lograter.logentry.IisLogEntry;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class IisLogFormatParserTest {

    @Test
    public void testParserDefaultFields() {

        String pattern = "#Fields: date time cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) sc-status sc-substatus sc-win32-status sc-bytes cs-bytes time-taken";

        List<LogbackElement> elements = IisLogFormatParser.parse(pattern);
        final int expectedElements = ((pattern.split(" ").length - 1) * 2);
        assertEquals("Expected every field, plus space between fields, plus final literal", expectedElements, elements.size());
        //System.out.println(elements);

        Map<String, LogEntryMapper<IisLogEntry>> mappers = IisLogEntry.initializeMappers(elements);
        IisLogFormatParser parser = new IisLogFormatParser(elements, mappers);

        String logline = "2012-11-09 06:30:12 POST /Webservices/NeverEndingStory/storyprovider.asmx - 443 - 1.2.3.4 HTTP/1.1 - - - 200 0 0 1475 1013 4515";
        IisLogEntry entry = parser.parseLogLine(logline);

        assertEquals(HttpMethod.POST, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("/Webservices/NeverEndingStory/storyprovider.asmx", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals(4515, entry.getDurationInMillis());

    }

    @Test
    public void testParserUnknownField() {

        String pattern = "#Fields: date time cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) sc-status s-event sc-win32-status sc-bytes cs-bytes time-taken";

        List<LogbackElement> elements = IisLogFormatParser.parse(pattern);

        Map<String, LogEntryMapper<IisLogEntry>> mappers = IisLogEntry.initializeMappers(elements);
        IisLogFormatParser parser = new IisLogFormatParser(elements, mappers);

        String logline = "2012-11-09 06:30:12 POST /Webservices/NeverEndingStory/storyprovider.asmx - 443 - 1.2.3.4 HTTP/1.1 - - - 200 test-event 0 1475 1013 4515";
        IisLogEntry entry = parser.parseLogLine(logline);

        assertEquals("test-event", entry.getField("s-event"));
	    assertEquals(4515, entry.getDurationInMillis());

    }

    @Test
    public void testParserHighValueBug() {

        // sc-win32-status can have 2148074254 as value, causing NumberFormatException
        String pattern = "#Fields: date time cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs-version cs(User-Agent) cs(Cookie) cs(Referer) sc-status s-event sc-win32-status sc-bytes cs-bytes time-taken";

        List<LogbackElement> elements = IisLogFormatParser.parse(pattern);

        Map<String, LogEntryMapper<IisLogEntry>> mappers = IisLogEntry.initializeMappers(elements);
        IisLogFormatParser parser = new IisLogFormatParser(elements, mappers);

        String logline = "2012-11-09 06:30:12 POST /Webservices/NeverEndingStory/storyprovider.asmx - 443 - 1.2.3.4 HTTP/1.1 - - - 200 test-event 2148074254 1475 1013 4515";
        IisLogEntry entry = parser.parseLogLine(logline);

        assertEquals("2148074254", entry.getField("sc-win32-status"));
	    assertEquals(4515, entry.getDurationInMillis());

    }

}
