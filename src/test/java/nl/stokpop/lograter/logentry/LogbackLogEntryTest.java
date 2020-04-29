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

import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackDirective;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.LogbackLiteral;
import nl.stokpop.lograter.parser.line.LogbackParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LogbackLogEntryTest {

    @Test
    public void testAllLogbackVariablesForMessage() {
        checkPatternMatching("m");
        checkPatternMatching("msg");
        checkPatternMatching("emsg");
        checkPatternMatching("message");
    }

    private void checkPatternMatching(String directive) {
        List<LogbackElement> elements = new ArrayList<>();
        elements.add(new LogbackLiteral(";"));
        elements.add(LogbackDirective.from(directive));
        elements.add(new LogbackLiteral(";"));
        Map<String, LogEntryMapper<LogbackLogEntry>> mapperMap = LogbackLogEntry.initializeLogBackMappers(elements);
        LogbackParser<LogbackLogEntry> parser = new LogbackParser<>(elements, mapperMap, LogbackLogEntry.class);

        String expectedMessage = "This is a message.";
        LogbackLogEntry logEntry = parser.parseLogLine(";" + expectedMessage + ";");
        assertEquals(expectedMessage, logEntry.getMessage());
    }

}