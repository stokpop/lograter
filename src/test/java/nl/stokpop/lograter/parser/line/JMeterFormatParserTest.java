/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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

import static org.junit.Assert.*;

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
        // note that label is mapped to getUrl for jmeter log lines!
        // in this older example, the URL field does not exist, unlike the more recent example below
        assertEquals("/delay", entry.getUrl());
        assertEquals("text", entry.getField("dataType"));
        assertTrue(entry.isSuccess());

        assertEquals(logline, entry.getLogline());
    }

    @Test
    public void parseWithQuotesAndCommas() {

        String pattern = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

        List<LogbackElement> elements = JMeterLogFormatParser.parse(pattern);
        final int expectedElements = 34;
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<JMeterLogEntry>> mappers = JMeterLogFormatParser.initializeMappers();
        JMeterLogFormatParser parser = new JMeterLogFormatParser(elements, mappers);

        String logline = "\"1616977368110\",911,\"01_getApp\",200,\"Number of samples in transaction : 1, number of failing samples : 0\",\"App-Test 1-200\",,true,,1126,734,1,1,null,0,145,\"5\"";
        JMeterLogEntry entry = parser.parseLogLine(logline);

        assertEquals(1616977368110L, entry.getTimestamp());
        assertEquals("1616977368110", entry.getField("timeStamp"));
        assertEquals(200, entry.getCode());
        // note that label is mapped to getUrl for jmeter log lines!
        assertEquals("01_getApp", entry.getUrl());
        assertEquals("null", entry.getField("URL"));
        assertEquals("1126", entry.getField("bytes"));
        assertEquals("145", entry.getField("IdleTime"));
        assertEquals("5", entry.getField("Connect"));
        assertTrue(entry.isSuccess());

        assertEquals(logline, entry.getLogline());
    }

    @Test
    public void parseWithQuotesAndCommasAndEscapedQuotes() {

        String pattern = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect";

        List<LogbackElement> elements = JMeterLogFormatParser.parse(pattern);
        final int expectedElements = 32;
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<JMeterLogEntry>> mappers = JMeterLogFormatParser.initializeMappers();
        JMeterLogFormatParser parser = new JMeterLogFormatParser(elements, mappers);

        // is this the exact failureMessage? Looks a bit weird...
        String logline = "1531213301741,34,stubby3/show,404,Not Found,Customers with Ramp Up and Down 1-159,text,false,\"Test failed: text should contain 'success'\":\\\"\"8917/\",483,194,200,200,34,0,0";
        JMeterLogEntry entry = parser.parseLogLine(logline);

        assertEquals(1531213301741L, entry.getTimestamp());
        assertEquals("1531213301741", entry.getField("timeStamp"));
        assertEquals(404, entry.getCode());
        // note that label is mapped to getUrl for jmeter log lines!
        assertEquals("stubby3/show", entry.getUrl());
        assertNull(entry.getField("URL"));
        assertEquals("483", entry.getField("bytes"));
        assertEquals("0", entry.getField("IdleTime"));
        assertEquals("0", entry.getField("Connect"));
        assertEquals("Test failed: text should contain 'success'\":\\\"\"8917/", entry.getField("failureMessage"));
        assertFalse(entry.isSuccess());

        assertEquals(logline, entry.getLogline());
    }
}