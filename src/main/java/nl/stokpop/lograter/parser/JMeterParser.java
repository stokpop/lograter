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
package nl.stokpop.lograter.parser;

import nl.stokpop.lograter.parser.line.JMeterLogFormatParser;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.processor.jmeter.JMeterLogEntry;

import java.util.ArrayList;
import java.util.List;

public class JMeterParser implements LogFileParser<JMeterLogEntry> {

    private final List<Processor<JMeterLogEntry>> processors = new ArrayList<>();

    private final JMeterLogFormatParser parser;

    public JMeterParser(JMeterLogFormatParser formatParser) {
        parser = formatParser;
    }

    @Override
    public void addProcessor(final Processor<JMeterLogEntry> processor) {
        processors.add(processor);
    }

    @Override
    public void addLogLine(final String filename, final String logLine) {

        JMeterLogEntry entry = parser.parseLogLine(logLine);

        // skip "transaction/sample" names that are not http hits
        String responseMessage = entry.getField("responseMessage");
        if (responseMessage != null && responseMessage.contains("Number of samples in transaction")) {
            return;
        }

        for (Processor<JMeterLogEntry> processor : processors) {
            processor.processEntry(entry);
        }
    }
}
