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

import nl.stokpop.lograter.processor.jmeter.JMeterLogEntry;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JMeterFormatParserTest {

    @Test
    public void parse() {

        String pattern = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect";

        List<LogbackElement> elements = JMeterLogFormatParser.parse(pattern);
        final int expectedElements = 32;
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<JMeterLogEntry>> mappers = JMeterLogFormatParser.initializeMappers();
        JMeterLogFormatParser parser = new JMeterLogFormatParser(elements, mappers);

        String logline = "525263894417,5834,/delay,200,OK,Thread Group 1-2,text,true,,381,140,5,5,5834,0,5632";
        JMeterLogEntry entry = parser.parseLogLine(logline);

        assertEquals(525263894417L, entry.getTimestamp());
        assertEquals("525263894417", entry.getField("timeStamp"));
        assertEquals(200, entry.getCode());
        assertEquals("/delay", entry.getUrl());
        assertEquals("text", entry.getDataType());
        assertTrue(entry.isSuccess());

        assertEquals(logline, entry.getLogline());
    }
}