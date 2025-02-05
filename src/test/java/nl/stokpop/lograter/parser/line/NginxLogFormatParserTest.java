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

import nl.stokpop.lograter.counter.HttpMethod;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.logentry.NginxLogMapperFactory;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NginxLogFormatParserTest {

    @Test
    public void parse() {

        String pattern = "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"";

        List<LogbackElement> elements = NginxLogFormatParser.parse(pattern);
        final int expectedElements = 17;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = NginxLogMapperFactory.initializeMappers(elements);
        NginxLogFormatParser<AccessLogEntry> parser = new NginxLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "10.239.208.214 - pp [12/Mar/2018:20:03:01 +0100] \"POST /story-api/createStory HTTP/1.1\" 400 192 \"-\" \"Apache-HttpClient/4.5 (Java/1.8.0_102)\"";
        AccessLogEntry entry = parser.parseLogLine(logline);

        System.out.println(entry);

        assertEquals("10.239.208.214", entry.getField("remote_addr"));
        assertEquals("12/Mar/2018:20:03:01 +0100", entry.getField("time_local"));
        assertEquals(HttpMethod.POST, entry.getHttpMethod());
        assertEquals(400, entry.getHttpStatus());
        assertEquals("/story-api/createStory", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals("Apache-HttpClient/4.5 (Java/1.8.0_102)", entry.getUserAgent());
    }
}