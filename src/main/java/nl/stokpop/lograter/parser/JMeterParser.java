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
package nl.stokpop.lograter.parser;

import net.jcip.annotations.NotThreadSafe;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.parser.line.JMeterLogFormatParser;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.processor.jmeter.JMeterLogEntry;
import nl.stokpop.lograter.processor.jmeter.JMeterLogLineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NotThreadSafe
public class JMeterParser implements LogFileParser<JMeterLogEntry> {

    private final static Logger log = LoggerFactory.getLogger(IisLogParser.class);
    public static final Instant MILLENIUM = Instant.parse("2000-01-01T00:00:00.00Z");

    private final List<Processor<JMeterLogEntry>> processors = new ArrayList<>();

    private final JMeterLogFormatParser parser;

    private final JMeterLogLineType logLineTypeToReport;

    private final List<String> incompleteLines = new ArrayList<>();

    public JMeterParser(JMeterLogFormatParser formatParser, JMeterLogLineType logLineTypeToReport) {
        this.parser = formatParser;
        this.logLineTypeToReport = logLineTypeToReport;
    }

    @Override
    public void addProcessor(final Processor<JMeterLogEntry> processor) {
        processors.add(processor);
    }

    @Override
    public void addLogLine(final String filename, final String logLine) {
        JMeterLogEntry entry = null;
        try {
            String lineToParse = null;
            if (incompleteLines.size() > 0) {
                // if starts with timestamp, reset incomplete lines
                String beforeFirstComma = logLine.contains(",") ? logLine.substring( 0, logLine.indexOf(",")) : logLine;
                boolean mightBeValidTimestamp;
                try {
                    long aLong = Long.parseLong(beforeFirstComma);
                    // check if valid timestamp
                    mightBeValidTimestamp = Instant.ofEpochMilli(aLong).isAfter(MILLENIUM);
                } catch (NumberFormatException e) {
                    mightBeValidTimestamp = false;
                }

                if (mightBeValidTimestamp) {
                    log.warn("Skipping potentially corrupt lines: " + incompleteLines);
                    incompleteLines.clear();
                    lineToParse = logLine;
                }
                else {
                    // check if line can be parsed
                    boolean failed = false;
                    try {
                        entry = parser.parseLogLine(logLine);
                    } catch (LogRaterException | NumberFormatException e) {
                        failed = true;
                    }
                    if (failed) {
                        lineToParse = String.join("", incompleteLines) + logLine;
                    }
                }
            }
            else {
                lineToParse = logLine;
            }
            // check if parse already succeeded
            if (entry == null) {
                entry = parser.parseLogLine(lineToParse);
            }
            // if no error, clear the incompleteLines
            incompleteLines.clear();
        } catch (LogRaterException | NumberFormatException e) {
            log.warn("adding incomplete line: " + e);
            incompleteLines.add(logLine);
        }

        if (incompleteLines.isEmpty() && entry != null) {
            // only report requested log line types
            if (logLineTypeToReport == JMeterLogLineType.ALL || entry.getLogLineType() == logLineTypeToReport) {
                for (Processor<JMeterLogEntry> processor : processors) {
                    processor.processEntry(entry);
                }
            }
        }

    }
}
