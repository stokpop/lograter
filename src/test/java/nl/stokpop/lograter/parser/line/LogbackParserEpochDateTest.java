/*
 * Copyright (C) 2026 Peter Paul Bakker, Stokpop
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

import nl.stokpop.lograter.logentry.LogbackLogEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogbackParserEpochDateTest {

    @Test
    public void testParseEpochMillis() {
        String pattern = "%d{epoch_millis} %m%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(pattern);

        String logline = "1712749095123 hello";
        LogbackLogEntry entry = parser.parseLogLine(logline);

        assertEquals(1712749095123L, entry.getTimestamp());
        assertEquals("hello", entry.getMessage());
    }

    @Test
    public void testParseEpochSeconds() {
        String pattern = "%d{epoch_seconds} %m%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(pattern);

        String logline = "1712749095 hello";
        LogbackLogEntry entry = parser.parseLogLine(logline);

        assertEquals(1712749095000L, entry.getTimestamp());
        assertEquals("hello", entry.getMessage());
    }
}

