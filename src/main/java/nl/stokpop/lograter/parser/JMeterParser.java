/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses JMeter lines.
 *
 * Can also deal with multi-lines: log entries that are split over multiple lines in the jtl file.
 * There are cases where messages are multi-line.
 *
 * So when it encounters a line it cannot parse, it will store that line and combine it with the
 * next line to see if it can be parsed by concatenation.
 *
 * It uses some heuristics to determine if next lines start with a proper time stamp and considers
 * those a valid new lines. It can potentially skip partial lines above that point.
 * These are logged as a warning.
 */
@NotThreadSafe
public class JMeterParser implements LogFileParser<JMeterLogEntry> {

    private final static Logger log = LoggerFactory.getLogger(IisLogParser.class);

    private static final Instant MILLENIUM = Instant.parse("2000-01-01T00:00:00.00Z");
    private static final Instant FUTURE = Instant.now().plus(Duration.ofDays(365));
    private static final Duration SEVEN_DAYS = Duration.ofDays(7);

    private final List<Processor<JMeterLogEntry>> processors = new ArrayList<>();

    private final JMeterLogFormatParser parser;

    private final JMeterLogLineType logLineTypeToReport;

    private final List<String> incompleteLines = new ArrayList<>();
    private Instant lastTimeStamp = FUTURE;

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

        String lineToParse;

        if (incompleteLines.isEmpty()) {
            // just use the provide log line
            lineToParse = logLine;
        } else {
            // alternative: check if line can be parsed
            //            entry = checkParseLine(logLine);
            //            if (entry != null) {
            //                incompleteLines.clear();
            //            }

            // If starts with timestamp, it might be start of new correct line, reset incomplete lines
            // can be tricky when a line contains a number that is similar to a possible timestamp
            // An alternative is to parseLines as above here, but you might miss valid lines
            // due to concatenation.
            boolean mightBeValidTimestamp = checkStartsWithPossibleValidTimestamp(logLine);

            if (mightBeValidTimestamp) {
                log.warn("Skipping potentially corrupt lines: " + incompleteLines);
                incompleteLines.clear();
            }

            // check if it can be parsed by combining previous failed lines
            lineToParse = String.join("", incompleteLines) + logLine;
        }

        // check if parse already succeeded, if not, go parse the (combined) line
        JMeterLogEntry entry = checkParseLine(lineToParse);

        if (entry != null) {
            // if no error, clear the incompleteLines
            incompleteLines.clear();
            if (isRequestedLogLineType(entry)) {
                for (Processor<JMeterLogEntry> processor : processors) {
                    processor.processEntry(entry);
                }
            }
        }
        else {
            log.debug("adding incomplete line: " + logLine);
            incompleteLines.add(logLine);
        }
    }

    private boolean isRequestedLogLineType(JMeterLogEntry entry) {
        return logLineTypeToReport == JMeterLogLineType.ALL || entry.getLogLineType() == logLineTypeToReport;
    }

    /**
     * Check if a log line can be parsed, if not, return null.
     */
    @Nullable
    private JMeterLogEntry checkParseLine(String logLine) {
        JMeterLogEntry testEntry;
        try {
            testEntry = parser.parseLogLine(logLine);
        } catch (LogRaterException | NumberFormatException e) {
            log.debug("cannot parse (partial) log line: " + e + ": " + logLine);
            testEntry = null;
        }
        return testEntry;
    }

    private boolean checkStartsWithPossibleValidTimestamp(String logLine) {
        String beforeFirstComma = logLine.contains(",") ? logLine.substring( 0, logLine.indexOf(",")) : logLine;
        boolean mightBeValidTimestamp;
        try {
            long timeStamp = Long.parseLong(beforeFirstComma);
            // check if valid timestamp
            Instant instant = Instant.ofEpochMilli(timeStamp);
            // check if new timestamp is within 7 days of last known timestamp
            mightBeValidTimestamp = instant.isAfter(MILLENIUM)
                && instant.isBefore(lastTimeStamp.plus(SEVEN_DAYS));

            lastTimeStamp = instant;

        } catch (NumberFormatException e) {
            mightBeValidTimestamp = false;
        }
        return mightBeValidTimestamp;
    }
}
